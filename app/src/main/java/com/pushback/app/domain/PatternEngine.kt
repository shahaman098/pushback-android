package com.pushback.app.domain

import com.pushback.app.data.BehaviorEventEntity
import com.pushback.app.data.DayPlanEntity
import com.pushback.app.data.DayReviewEntity
import com.pushback.app.data.FocusSessionEntity
import com.pushback.app.data.JobEntity
import com.pushback.app.data.Settings
import com.pushback.app.data.SettingsStore
import com.pushback.app.data.UsageSliceEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class PatternEngine {
    fun detect(
        now: LocalDate,
        settings: Settings,
        plans: List<DayPlanEntity>,
        reviews: List<DayReviewEntity>,
        events: List<BehaviorEventEntity>,
        jobs: List<JobEntity>,
        usage: List<UsageSliceEntity>,
        focus: List<FocusSessionEntity>,
        daysSinceInstall: Long,
    ): DetectedPattern? {
        if (daysSinceInstall < 7) return null

        val windowStart = now.minusDays(13)
        val recentPlans = plans.filter { Dates.parse(it.date) >= windowStart }
        val recentEvents = events.filter {
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() >= windowStart
        }
        val recentUsage = usage.filter {
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() >= windowStart
        }

        return listOfNotNull(
            quotaMisfit(recentPlans, settings),
            peakWindow(recentEvents, settings),
            driftWindow(recentUsage),
            fakeWork(recentUsage, recentEvents),
            avoidance(recentEvents, recentPlans),
            eveningLag(recentPlans, reviews, now),
            ifThenMismatch(recentPlans, recentEvents),
        ).firstOrNull()
    }

    private fun quotaMisfit(plans: List<DayPlanEntity>, settings: Settings): DetectedPattern? {
        val last7 = plans.sortedByDescending { it.date }.take(7)
        if (last7.size < 5) return null
        val misses = last7.count { !it.quotaHit }
        val earlyHits = last7.count { plan ->
            plan.quotaHitAt != null && Dates.hourOf(plan.quotaHitAt) < 12
        }
        return when {
            misses >= 4 && settings.quota > settings.quotaFloor -> {
                val next = (settings.quota - 1).coerceAtLeast(settings.quotaFloor)
                DetectedPattern(
                    claim = "You missed quota on $misses of the last ${last7.size} days. The quota is too high to be real.",
                    evidence = last7.map { "${it.date}: sent ${it.sentCount}/${it.quotaTarget}" },
                    failureMode = FailureMode.NO_SEND,
                    proposed = ProtocolChange(
                        title = "Drop daily applications to $next for 7 days",
                        detail = "Hit a smaller number every day. Raise it when you are hitting before noon.",
                        quota = next,
                    ),
                )
            }
            earlyHits >= 5 && settings.quota < settings.quotaCeiling -> {
                val next = (settings.quota + 1).coerceAtMost(settings.quotaCeiling)
                DetectedPattern(
                    claim = "You hit quota before noon on $earlyHits of the last ${last7.size} days. Raise the standard.",
                    evidence = last7.map { "${it.date}: sent ${it.sentCount}/${it.quotaTarget}" },
                    failureMode = FailureMode.FAKE_WORK,
                    proposed = ProtocolChange(
                        title = "Raise daily applications to $next",
                        detail = "You already clear the bar early. Compound the surplus into more sends, then leverage.",
                        quota = next,
                    ),
                )
            }
            else -> null
        }
    }

    private fun peakWindow(events: List<BehaviorEventEntity>, settings: Settings): DetectedPattern? {
        val sends = events.filter { it.type == EventType.SEND }
        if (sends.size < 6) return null
        val hours = sends.map { Dates.hourOf(it.timestamp) }
        val best = hours.groupingBy { it }.eachCount().maxByOrNull { it.value } ?: return null
        if (best.value * 100 / sends.size < 40) return null
        val lock = best.key
        if (settings.frogLockHour == lock) return null
        return DetectedPattern(
            claim = "You send ${best.value * 100 / sends.size}% of applications around ${Dates.formatTime(lock, 0)}.",
            evidence = listOf("${sends.size} sends in the last two weeks", "Peak hour $lock:00"),
            failureMode = FailureMode.AVOIDANCE,
            proposed = ProtocolChange(
                title = "Lock the Frog at ${Dates.formatTime(lock, 0)}",
                detail = "Stop pretending mornings are your send window if the data says otherwise. Protect the hour you already use.",
                frogLockHour = lock,
                ifThen = "If the clock hits ${Dates.formatTime(lock, 0)}, then I open PushBack and send the next application before I touch anything else.",
            ),
        )
    }

    private fun driftWindow(usage: List<UsageSliceEntity>): DetectedPattern? {
        val open = usage.filter { it.requiredWasOpen && it.minutes >= 1f }
        if (open.size < 8) return null
        val byHourPkg = open.groupBy { Dates.hourOf(it.timestamp) to it.packageName }
            .mapValues { e -> e.value.sumOf { it.minutes.toDouble() } }
        val worst = byHourPkg.maxByOrNull { it.value } ?: return null
        val (hour, pkg) = worst.key
        val name = SettingsStore.KNOWN_WASTE[pkg] ?: pkg
        val days = open.map {
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).dayOfWeek
        }.groupingBy { it }.eachCount()
        val heavyDays = days.filter { it.value >= 2 }.keys.sorted()
        val dayLabel = if (heavyDays.isEmpty()) "this week" else heavyDays.joinToString { it.short() }
        return DetectedPattern(
            claim = "$dayLabel around ${Dates.formatTime(hour, 0)}, $name while quota is still open.",
            evidence = listOf(
                "${"%.0f".format(worst.value)} minutes of $name during unfinished Required",
                "Kill zone ${Dates.formatTime(hour, 0)}–${Dates.formatTime((hour + 2) % 24, 0)}",
            ),
            failureMode = FailureMode.SCROLLING,
            proposed = ProtocolChange(
                title = "Treat ${Dates.formatTime(hour, 0)} as a kill zone",
                detail = "Stricter watchdog. Nudge 15 minutes before the drift. $name is not the job.",
                killZoneStart = hour,
                killZoneEnd = (hour + 2) % 24,
                nudgeHour = (hour + 23) % 24,
                nudgeMinute = 45,
                ifThen = "If I open $name after ${Dates.formatTime(hour, 0)}, then I close it and send one application.",
            ),
        )
    }

    private fun fakeWork(usage: List<UsageSliceEntity>, events: List<BehaviorEventEntity>): DetectedPattern? {
        val linkedin = usage.filter {
            it.packageName.contains("linkedin", ignoreCase = true) && it.requiredWasOpen
        }.sumOf { it.minutes.toDouble() }
        val sends = events.count { it.type == EventType.SEND }
        if (linkedin < 40) return null
        if (sends >= 8) return null
        return DetectedPattern(
            claim = "LinkedIn was open ${linkedin.toInt()} minutes with almost no status change to applied. That is fake work.",
            evidence = listOf("$sends sends in the window", "${linkedin.toInt()} minutes on LinkedIn while Required was open"),
            failureMode = FailureMode.FAKE_WORK,
            proposed = ProtocolChange(
                title = "Cap research. Count only sends.",
                detail = "Timeboxing applications is the point. Browsing is not a pipeline.",
                ifThen = "If I open LinkedIn, then I send one application within 20 minutes or I close it.",
            ),
        )
    }

    private fun avoidance(events: List<BehaviorEventEntity>, plans: List<DayPlanEntity>): DetectedPattern? {
        val postpones = events.filter { it.type == EventType.POSTPONE }
        val last5 = plans.sortedByDescending { it.date }.take(5)
        if (postpones.size < 4 && last5.count { it.morningDoneAt == null } < 3) return null
        return DetectedPattern(
            claim = "You postponed the Frog or skipped the morning briefing repeatedly. Avoidance is the pattern, not a mood.",
            evidence = listOf(
                "${postpones.size} postpones in two weeks",
                "${last5.count { it.morningDoneAt == null }} of last ${last5.size} mornings unfinished",
            ),
            failureMode = FailureMode.AVOIDANCE,
            proposed = ProtocolChange(
                title = "Make the first action a send, not a plan",
                detail = "WOOP stays. The Frog is 'send one application' before any other task exists.",
                ifThen = "If I unlock my phone after waking, then I open PushBack and send one application before anything else.",
            ),
        )
    }

    private fun eveningLag(
        plans: List<DayPlanEntity>,
        reviews: List<DayReviewEntity>,
        now: LocalDate,
    ): DetectedPattern? {
        if (plans.size < 6) return null
        val reviewDates = reviews.map { it.date }.toSet()
        var skipThenMiss = 0
        var skipThenHit = 0
        plans.forEach { plan ->
            val date = Dates.parse(plan.date)
            val prev = Dates.iso(date.minusDays(1))
            if (prev !in reviewDates && date < now) {
                if (plan.quotaHit) skipThenHit++ else skipThenMiss++
            }
        }
        if (skipThenMiss < 3) return null
        if (skipThenMiss <= skipThenHit) return null
        return DetectedPattern(
            claim = "Skip the evening review and the next day you miss quota ($skipThenMiss misses vs $skipThenHit hits).",
            evidence = listOf("After-action review is not optional. It is the instrument panel."),
            failureMode = FailureMode.NO_REVIEW,
            proposed = ProtocolChange(
                title = "Treat the evening review as Required",
                detail = "Eight minutes. Supposed, happened, well, wrong, tomorrow's if-then.",
                ifThen = "If it is 21:00, then I open PushBack and finish the after-action review before any other app.",
            ),
        )
    }

    private fun ifThenMismatch(plans: List<DayPlanEntity>, events: List<BehaviorEventEntity>): DetectedPattern? {
        val firstSends = events.filter { it.type == EventType.SEND }
            .groupBy {
                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            }
            .mapValues { e -> e.value.minBy { it.timestamp } }
        var mismatches = 0
        val notes = mutableListOf<String>()
        plans.filter { it.ifThen.contains("9:00") || it.ifThen.contains("09:00") }.forEach { plan ->
            val date = Dates.parse(plan.date)
            val first = firstSends[date] ?: return@forEach
            val hour = Dates.hourOf(first.timestamp)
            if (hour >= 15) {
                mismatches++
                notes += "${plan.date}: wrote 9:00, first send ${Dates.formatTime(hour, 0)}"
            }
        }
        if (mismatches < 3) return null
        return DetectedPattern(
            claim = "Your if-then says morning. Your first send is afternoon. Stop lying to the plan.",
            evidence = notes.take(5),
            failureMode = FailureMode.AVOIDANCE,
            proposed = ProtocolChange(
                title = "Rewrite the if-then to the hour you actually send",
                detail = "Implementation intentions only work if the cue is real.",
                ifThen = "If it is 16:00 and quota is open, then I send the next application immediately.",
            ),
        )
    }

    private fun DayOfWeek.short(): String = name.take(3).lowercase().replaceFirstChar { it.uppercase() }
}
