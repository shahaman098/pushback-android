package com.pushback.app.data

import com.pushback.app.domain.Dates
import com.pushback.app.domain.EventType
import com.pushback.app.domain.FailureMode
import com.pushback.app.domain.JobStatus
import com.pushback.app.domain.TaskTrack
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object DemoDataSeeder {
    suspend fun seed(db: PushBackDatabase, settingsStore: SettingsStore) {
        val today = Dates.today()
        val now = System.currentTimeMillis()

        // 1. Seed sample jobs across various pipeline stages
        val sampleJobs = listOf(
            JobEntity(
                company = "Anthropic",
                role = "Product Infrastructure Engineer",
                url = "https://jobs.lever.co/anthropic/infrastructure",
                status = JobStatus.INTERVIEW,
                createdAt = now - TimeUnit.DAYS.toMillis(8),
                appliedAt = now - TimeUnit.DAYS.toMillis(8),
                notes = "Screen passed. Technical interview scheduled for Friday 2 PM.",
            ),
            JobEntity(
                company = "Linear",
                role = "Senior Android Engineer",
                url = "https://linear.app/careers/android",
                status = JobStatus.OFFER,
                createdAt = now - TimeUnit.DAYS.toMillis(12),
                appliedAt = now - TimeUnit.DAYS.toMillis(11),
                notes = "Offer received. Reviewing equity package.",
            ),
            JobEntity(
                company = "Stripe",
                role = "Staff Mobile Engineer",
                url = "https://stripe.com/jobs/mobile-staff",
                status = JobStatus.APPLIED,
                createdAt = now - TimeUnit.DAYS.toMillis(4),
                appliedAt = now - TimeUnit.DAYS.toMillis(3),
                notes = "Sent tailored portfolio via referral.",
            ),
            JobEntity(
                company = "Figma",
                role = "Systems Engineer",
                url = "https://boards.greenhouse.io/figma/jobs/92819",
                status = JobStatus.TO_APPLY,
                createdAt = now - TimeUnit.DAYS.toMillis(2),
                notes = "Priority listing. Tailor pitch around canvas rendering.",
            ),
            JobEntity(
                company = "Vercel",
                role = "Developer Experience Lead",
                url = "https://vercel.com/careers/dx-lead",
                status = JobStatus.TO_APPLY,
                createdAt = now - TimeUnit.DAYS.toMillis(1),
                notes = "Next in queue.",
            ),
            JobEntity(
                company = "GitHub",
                role = "Senior Core Systems Engineer",
                url = "https://github.com/careers",
                status = JobStatus.FOLLOWED_UP,
                createdAt = now - TimeUnit.DAYS.toMillis(7),
                appliedAt = now - TimeUnit.DAYS.toMillis(6),
                notes = "Followed up with engineering manager on LinkedIn.",
            ),
            JobEntity(
                company = "Notion",
                role = "Client Infrastructure Engineer",
                url = "https://notion.so/careers",
                status = JobStatus.REJECTED,
                createdAt = now - TimeUnit.DAYS.toMillis(14),
                appliedAt = now - TimeUnit.DAYS.toMillis(13),
                notes = "Position closed internally.",
            ),
        )

        sampleJobs.forEach { db.jobs().insert(it) }

        // 2. Seed 14 days of DayPlans and DayReviews
        for (i in 13 downTo 1) {
            val date = today.minusDays(i.toLong())
            val dateIso = Dates.iso(date)
            val quota = 5
            val sent = when (i % 4) {
                0 -> 5
                1 -> 6
                2 -> 3
                else -> 5
            }
            val hit = sent >= quota

            db.plans().upsert(
                DayPlanEntity(
                    date = dateIso,
                    identityLine = "I am a person who sends the work, then builds leverage.",
                    wish = "Send $quota targeted applications before noon.",
                    outcome = "Uninterrupted afternoon compound block on leverage.",
                    obstacle = "Procrastinating on portfolio polishing.",
                    plan = "If I feel resistance, I open PushBack and send the lead listing immediately.",
                    quotaTarget = quota,
                    sentCount = sent,
                    quotaHit = hit,
                    quotaHitAt = if (hit) Dates.startOfDay(date) + TimeUnit.HOURS.toMillis(11) else null,
                    ifThen = "If it is 09:00, then I send 5 targeted applications before opening anything else.",
                    morningDoneAt = Dates.startOfDay(date) + TimeUnit.HOURS.toMillis(8),
                    patternLine = if (i <= 5) "Peak send window identified around 10:00 AM." else "",
                ),
            )

            db.reviews().upsert(
                DayReviewEntity(
                    date = dateIso,
                    supposed = "Send $quota applications. Frog first. 90m compound block.",
                    happened = if (hit) "Sent $sent applications. Completed 90m on mobile engine." else "Sent $sent applications. Drank coffee and browsed tabs.",
                    wentWell = if (hit) "Cleared the frog early by 11:00 AM. Zero social media drift." else "Saved 2 high quality listings for tomorrow.",
                    wentWrong = if (hit) "Slightly delayed start due to email checking." else "Fell into LinkedIn feed trap for 45 minutes instead of sending.",
                    tomorrowIfThen = "If the clock strikes 09:00, I will send application #1 before checking messages.",
                    mentalityId = if (hit) "grove-output" else "munger-stupid",
                    createdAt = Dates.startOfDay(date) + TimeUnit.HOURS.toMillis(21),
                ),
            )

            // Behavior events
            repeat(sent) { sIdx ->
                db.events().insert(
                    BehaviorEventEntity(
                        type = EventType.SEND,
                        timestamp = Dates.startOfDay(date) + TimeUnit.HOURS.toMillis((9 + sIdx).toLong()),
                    ),
                )
            }
        }

        // 3. Seed pending PatternInsight
        db.insights().insert(
            PatternInsightEntity(
                windowStart = Dates.iso(today.minusDays(13)),
                claim = "You send 68% of applications between 09:00 and 11:00 AM. Afternoons have 3.4x higher distraction drift.",
                evidenceJson = "[\"28 sends logged between 09:00–11:30 AM\", \"Peak focus recorded on 9 out of last 14 mornings\", \"Afternoon kill-zone identified around 15:00 on Reddit & YouTube\"]",
                failureMode = FailureMode.AVOIDANCE,
                proposedTitle = "Lock the Morning Frog Protocol to 09:00 AM",
                proposedDetail = "Protect your proven high-output window. Shift all deep application work to the morning hours and compound in the late afternoon.",
                proposedIfThen = "If it is 09:00 AM, then I open PushBack and clear the 5 daily applications before opening any browser tab.",
                proposedFrogLockHour = 9,
                proposedKillStart = 14,
                proposedKillEnd = 16,
            ),
        )

        // 4. Update settings
        settingsStore.update {
            it.copy(
                firstOpenAt = now - TimeUnit.DAYS.toMillis(14),
                onboardingDone = true,
                outcome = "Land a senior engineering role and ship 1 leveraged product.",
                quota = 5,
            )
        }
    }
}
