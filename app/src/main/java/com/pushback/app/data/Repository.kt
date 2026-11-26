package com.pushback.app.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import com.pushback.app.domain.Dates
import com.pushback.app.domain.DetectedPattern
import com.pushback.app.domain.EventType
import com.pushback.app.domain.FailureMode
import com.pushback.app.domain.JobStatus
import com.pushback.app.domain.MentalityEngine
import com.pushback.app.domain.PatternEngine
import com.pushback.app.domain.TaskTrack
import com.pushback.app.domain.TodaySnapshot
import com.pushback.app.notify.AlarmScheduler
import com.pushback.app.notify.CatchupWorker
import com.pushback.app.notify.CoachNotifications
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class Repository(
    private val context: Context,
    private val db: PushBackDatabase,
    val settings: SettingsStore,
    val mentality: MentalityEngine,
    private val engine: PatternEngine = PatternEngine(),
    private val alarms: AlarmScheduler = AlarmScheduler(context),
) {
    val jobs: Flow<List<JobEntity>> = db.jobs().observeAll()
    val allPlans: Flow<List<DayPlanEntity>> = db.plans().observeAll()
    val allReviews: Flow<List<DayReviewEntity>> = db.reviews().observeAll()
    val pendingInsight: Flow<PatternInsightEntity?> = db.insights().observePending()
    val insights: Flow<List<PatternInsightEntity>> = db.insights().observeAll()
    val activeFocus: Flow<FocusSessionEntity?> = db.focus().observeActive()

    fun tasksFor(date: String = Dates.todayIso()): Flow<List<TaskEntity>> =
        db.tasks().observeForDate(date)

    fun planFor(date: String = Dates.todayIso()): Flow<DayPlanEntity?> = db.plans().observe(date)
    fun reviewFor(date: String = Dates.todayIso()): Flow<DayReviewEntity?> = db.reviews().observe(date)

    fun todaySnapshot(): Flow<TodaySnapshot> = combine(
        combine(settings.flow, jobs, tasksFor(), ::Triple),
        combine(planFor(), reviewFor(), pendingInsight, ::Triple),
    ) { a, b ->
        buildSnapshot(a.first, a.second, a.third, b.first, b.second, b.third)
    }

    private suspend fun buildSnapshot(
        s: Settings,
        jobList: List<JobEntity>,
        tasks: List<TaskEntity>,
        plan: DayPlanEntity?,
        review: DayReviewEntity?,
        insight: PatternInsightEntity?,
    ): TodaySnapshot {
        val today = Dates.today()
        val from = Dates.startOfDay(today)
        val to = Dates.endOfDay(today)
        val sent = jobList.count { it.appliedAt != null && it.appliedAt in from..to }
        val target = plan?.quotaTarget ?: s.quota
        val remaining = (target - sent).coerceAtLeast(0)
        val frog = tasks.firstOrNull { it.isFrog } ?: tasks.firstOrNull { it.track == TaskTrack.REQUIRED && it.doneAt == null }
        val compound = tasks.firstOrNull { it.track == TaskTrack.COMPOUND }
        val week = db.plans().since(Dates.iso(today.minusDays(6)))
        val hit = week.count { it.quotaHit }
        val debt = db.tasks().debt(Dates.todayIso())
        val next = nextNotificationLabel(s, remaining, review != null)
        val nextJob = jobList
            .filter { it.status == JobStatus.TO_APPLY }
            .minByOrNull { it.createdAt }
        return TodaySnapshot(
            date = today,
            identityLine = plan?.identityLine ?: s.identityLine,
            outcome = s.outcome,
            quotaTarget = target,
            sentToday = sent,
            requiredRemaining = remaining,
            frogTitle = frog?.title,
            frogDone = frog?.doneAt != null,
            compoundLocked = remaining > 0,
            compoundTitle = compound?.title,
            compoundDone = compound?.doneAt != null,
            consistency = if (week.isEmpty()) 0f else hit.toFloat() / week.size,
            daysHit = hit,
            daysTracked = week.size,
            debtCount = debt.size,
            morningDone = plan?.morningDoneAt != null,
            eveningDone = review != null,
            ifThen = plan?.ifThen.orEmpty(),
            patternLine = insight?.claim ?: plan?.patternLine?.ifBlank { null },
            nextNotificationLabel = next,
            watchdogOn = s.watchdogEnabled,
            usageGranted = hasUsageAccess(),
            now = java.time.LocalDateTime.now(),
            nextJobId = nextJob?.id,
            nextJobCompany = nextJob?.company,
            nextJobRole = nextJob?.role,
            nextJobUrl = nextJob?.url,
            waitingToApply = jobList.count { it.status == JobStatus.TO_APPLY },
        )
    }

    private fun nextNotificationLabel(s: Settings, remaining: Int, evening: Boolean): String =
        when {
            remaining > 0 -> "Still due. I will keep naming the number."
            !evening -> "Evening review ${Dates.formatTime(s.eveningHour, s.eveningMinute)}"
            else -> "Day closed. Protect sleep."
        }

    suspend fun ensureTodayPlan() {
        val s = settings.current()
        if (s.firstOpenAt == 0L) {
            settings.update { it.copy(firstOpenAt = System.currentTimeMillis()) }
        }
        val date = Dates.todayIso()
        if (db.plans().get(date) == null) {
            val insight = db.insights().latest()?.takeIf { it.acceptedAt != null }
            db.plans().upsert(
                DayPlanEntity(
                    date = date,
                    identityLine = s.identityLine,
                    quotaTarget = s.quota,
                    ifThen = insight?.proposedIfThen.orEmpty(),
                    patternLine = insight?.claim.orEmpty(),
                ),
            )
        }
        val tasks = db.tasks().forDate(date)
        if (tasks.none { it.track == TaskTrack.REQUIRED }) {
            db.tasks().insert(
                TaskEntity(
                    title = "Send ${s.quota} targeted applications",
                    track = TaskTrack.REQUIRED,
                    dueDate = date,
                    isFrog = true,
                ),
            )
        }
        if (tasks.none { it.track == TaskTrack.COMPOUND }) {
            db.tasks().insert(
                TaskEntity(
                    title = "Compound block: skill, product, or outreach (${s.compoundMinutes}m)",
                    track = TaskTrack.COMPOUND,
                    dueDate = date,
                ),
            )
        }
        rolloverDebt()
        alarms.scheduleAll(settings.current())
        CatchupWorker.schedule(context)
        maybeRunPatternEngine()
    }

    private suspend fun rolloverDebt() {
        val yesterday = Dates.iso(Dates.today().minusDays(1))
        val leftover = db.tasks().forDate(yesterday)
            .filter { it.track == TaskTrack.REQUIRED && it.doneAt == null }
        leftover.forEach { task ->
            val already = db.tasks().forDate(Dates.todayIso()).any { it.title == "DEBT: ${task.title}" }
            if (!already) {
                db.tasks().insert(
                    task.copy(
                        id = 0,
                        title = "DEBT: ${task.title}",
                        dueDate = Dates.todayIso(),
                        isFrog = false,
                        postponedReason = "Rolled from $yesterday",
                    ),
                )
            }
        }
        val yPlan = db.plans().get(yesterday)
        if (yPlan != null && yPlan.morningDoneAt == null) {
            db.events().insert(BehaviorEventEntity(type = EventType.MORNING_SKIP, payload = yesterday))
        }
    }

    suspend fun completeMorning(
        wish: String,
        outcome: String,
        obstacle: String,
        planText: String,
        ifThen: String,
        frogTitle: String,
        identity: String,
    ) {
        val date = Dates.todayIso()
        val existing = db.plans().get(date) ?: return
        val s = settings.current()
        db.tasks().clearFrog(date)
        val frogId = db.tasks().insert(
            TaskEntity(title = frogTitle, track = TaskTrack.REQUIRED, dueDate = date, isFrog = true),
        )
        db.plans().update(
            existing.copy(
                identityLine = identity,
                wish = wish,
                outcome = outcome,
                obstacle = obstacle,
                plan = planText,
                frogTaskId = frogId,
                ifThen = ifThen,
                morningDoneAt = System.currentTimeMillis(),
                quotaTarget = s.quota,
            ),
        )
        db.events().insert(BehaviorEventEntity(type = EventType.MORNING_DONE))
        settings.update { it.copy(identityLine = identity) }
    }

    suspend fun completeEvening(
        supposed: String,
        happened: String,
        well: String,
        wrong: String,
        tomorrowIfThen: String,
    ) {
        val snap = todaySnapshot().first()
        val mode = mentality.inferMode(
            sentToday = snap.sentToday,
            quota = snap.quotaTarget,
            eveningDone = false,
            wasteInterrupts = db.events().ofTypeSince(
                EventType.WASTE_INTERRUPT,
                Dates.startOfDay(),
            ).size,
            postponed = db.events().ofTypeSince(EventType.POSTPONE, Dates.startOfDay()).isNotEmpty(),
        )
        val card = mentality.forMode(mode)
        db.reviews().upsert(
            DayReviewEntity(
                date = Dates.todayIso(),
                supposed = supposed,
                happened = happened,
                wentWell = well,
                wentWrong = wrong,
                tomorrowIfThen = tomorrowIfThen,
                mentalityId = card.id,
            ),
        )
        db.events().insert(BehaviorEventEntity(type = EventType.EVENING_DONE))
        val plan = db.plans().get(Dates.iso(Dates.today().plusDays(1)))
        if (plan == null) {
            val s = settings.current()
            db.plans().upsert(
                DayPlanEntity(
                    date = Dates.iso(Dates.today().plusDays(1)),
                    identityLine = s.identityLine,
                    quotaTarget = s.quota,
                    ifThen = tomorrowIfThen,
                ),
            )
        }
    }

    suspend fun addJob(company: String, role: String, url: String) {
        db.jobs().insert(JobEntity(company = company, role = role, url = url))
    }

    suspend fun updateJobStatus(job: JobEntity, status: JobStatus) {
        val now = System.currentTimeMillis()
        val appliedAt = when {
            status == JobStatus.TO_APPLY -> null
            job.appliedAt != null -> job.appliedAt
            else -> now
        }
        db.jobs().update(job.copy(status = status, appliedAt = appliedAt))
        if (status != JobStatus.TO_APPLY && job.status == JobStatus.TO_APPLY) {
            onSend()
        }
    }

    suspend fun deleteJob(id: Long) = db.jobs().delete(id)

    suspend fun markJobSent(id: Long) {
        val job = db.jobs().all().find { it.id == id } ?: return
        updateJobStatus(job, JobStatus.APPLIED)
    }

    suspend fun skipJob(id: Long) {
        val job = db.jobs().all().find { it.id == id } ?: return
        db.jobs().update(job.copy(createdAt = System.currentTimeMillis()))
    }

    private suspend fun onSend() {
        db.events().insert(BehaviorEventEntity(type = EventType.SEND))
        val date = Dates.todayIso()
        val plan = db.plans().get(date) ?: return
        val sent = db.jobs().appliedBetween(Dates.startOfDay(), Dates.endOfDay()).size
        val hit = sent >= plan.quotaTarget
        db.plans().update(
            plan.copy(
                sentCount = sent,
                quotaHit = hit,
                quotaHitAt = if (hit) plan.quotaHitAt ?: System.currentTimeMillis() else null,
            ),
        )
        if (hit && !plan.quotaHit) {
            db.events().insert(BehaviorEventEntity(type = EventType.QUOTA_HIT, payload = sent.toString()))
            val frog = db.tasks().forDate(date).firstOrNull { it.isFrog && it.doneAt == null }
            if (frog != null) db.tasks().update(frog.copy(doneAt = System.currentTimeMillis()))
        }
        val left = (plan.quotaTarget - sent).coerceAtLeast(0)
        CoachNotifications.stickyQuota(context, left)
        if (left == 0) {
            com.pushback.app.usage.WatchdogService.stop(context)
        }
    }

    suspend fun addTask(title: String, track: TaskTrack, frog: Boolean = false) {
        val date = Dates.todayIso()
        if (frog) db.tasks().clearFrog(date)
        db.tasks().insert(TaskEntity(title = title, track = track, dueDate = date, isFrog = frog))
    }

    suspend fun toggleTask(task: TaskEntity) {
        db.tasks().update(task.copy(doneAt = if (task.doneAt == null) System.currentTimeMillis() else null))
    }

    suspend fun postponeTask(task: TaskEntity, reason: String) {
        db.tasks().update(task.copy(postponedReason = reason, dueDate = Dates.iso(Dates.today().plusDays(1))))
        db.events().insert(BehaviorEventEntity(type = EventType.POSTPONE, payload = reason))
    }

    suspend fun deleteTask(id: Long) = db.tasks().delete(id)

    suspend fun startFocus(taskId: Long?, minutes: Int): Long {
        db.events().insert(BehaviorEventEntity(type = EventType.FOCUS_START))
        return db.focus().insert(
            FocusSessionEntity(
                taskId = taskId,
                startedAt = System.currentTimeMillis(),
                plannedMinutes = minutes,
            ),
        )
    }

    suspend fun endFocus(id: Long) {
        val session = db.focus().get(id) ?: return
        db.focus().update(session.copy(endedAt = System.currentTimeMillis()))
        db.events().insert(BehaviorEventEntity(type = EventType.FOCUS_END))
    }

    suspend fun bumpWasteInterrupt(sessionId: Long?) {
        db.events().insert(BehaviorEventEntity(type = EventType.WASTE_INTERRUPT))
        if (sessionId != null) {
            val session = db.focus().get(sessionId) ?: return
            db.focus().update(session.copy(interruptedByWasteCount = session.interruptedByWasteCount + 1))
        }
    }

    suspend fun logUsage(packageName: String, minutes: Float, requiredOpen: Boolean) {
        db.usage().insert(
            UsageSliceEntity(
                timestamp = System.currentTimeMillis(),
                packageName = packageName,
                minutes = minutes,
                requiredWasOpen = requiredOpen,
            ),
        )
    }

    suspend fun logNotifDismissed(kind: String) {
        db.events().insert(BehaviorEventEntity(type = EventType.NOTIF_DISMISSED, payload = kind))
    }

    suspend fun requiredOpen(): Boolean {
        val plan = db.plans().get(Dates.todayIso())
        val s = settings.current()
        val sent = db.jobs().appliedBetween(Dates.startOfDay(), Dates.endOfDay()).size
        val target = plan?.quotaTarget ?: s.quota
        return sent < target
    }

    suspend fun remainingRequired(): Int {
        val plan = db.plans().get(Dates.todayIso())
        val s = settings.current()
        val sent = db.jobs().appliedBetween(Dates.startOfDay(), Dates.endOfDay()).size
        return ((plan?.quotaTarget ?: s.quota) - sent).coerceAtLeast(0)
    }

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun currentForegroundPackage(): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, end - 60_000, end) ?: return null
        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    suspend fun acceptInsight(insight: PatternInsightEntity) {
        db.insights().update(insight.copy(acceptedAt = System.currentTimeMillis()))
        settings.update { s ->
            s.copy(
                quota = insight.proposedQuota ?: s.quota,
                frogLockHour = insight.proposedFrogLockHour ?: s.frogLockHour,
                killZoneStart = insight.proposedKillStart ?: s.killZoneStart,
                killZoneEnd = insight.proposedKillEnd ?: s.killZoneEnd,
                nudgeHour = insight.proposedNudgeHour ?: s.nudgeHour,
                nudgeMinute = insight.proposedNudgeMinute ?: s.nudgeMinute,
            )
        }
        val tomorrow = Dates.iso(Dates.today().plusDays(1))
        val existing = db.plans().get(tomorrow)
        if (existing != null && insight.proposedIfThen.isNotBlank()) {
            db.plans().update(existing.copy(ifThen = insight.proposedIfThen, patternLine = insight.claim))
        }
        alarms.scheduleAll(settings.current())
    }

    suspend fun rejectInsight(insight: PatternInsightEntity) {
        db.insights().update(insight.copy(rejectedAt = System.currentTimeMillis()))
    }

    suspend fun updateSettings(transform: (Settings) -> Settings) {
        settings.update(transform)
        alarms.scheduleAll(settings.current())
    }

    suspend fun finishOnboarding(
        outcome: String,
        quota: Int,
        identity: String,
        ifThen: String,
        waste: Set<String>,
    ) {
        settings.update {
            it.copy(
                onboardingDone = true,
                outcome = outcome,
                quota = quota.coerceIn(1, 12),
                identityLine = identity,
                wastePackages = waste,
                firstOpenAt = if (it.firstOpenAt == 0L) System.currentTimeMillis() else it.firstOpenAt,
            )
        }
        val date = Dates.todayIso()
        val existing = db.plans().get(date)
        if (existing != null) {
            db.plans().update(existing.copy(ifThen = ifThen, identityLine = identity, quotaTarget = quota))
        }
        ensureTodayPlan()
        alarms.scheduleAll(settings.current())
    }

    suspend fun maybeRunPatternEngine() {
        val s = settings.current()
        if (!s.patternAdaptation) return
        val weekMs = TimeUnit.DAYS.toMillis(6)
        if (db.insights().countSince(System.currentTimeMillis() - weekMs) > 0) return
        val pending = db.insights().observePending().first()
        if (pending != null) return
        val days = if (s.firstOpenAt == 0L) 0 else TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - s.firstOpenAt)
        val detected = engine.detect(
            now = Dates.today(),
            settings = s,
            plans = db.plans().since(Dates.iso(Dates.today().minusDays(21))),
            reviews = db.reviews().since(Dates.iso(Dates.today().minusDays(21))),
            events = db.events().since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(21)),
            jobs = db.jobs().all(),
            usage = db.usage().since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(21)),
            focus = db.focus().recent(),
            daysSinceInstall = days,
        ) ?: return
        insertInsight(detected)
    }

    private suspend fun insertInsight(detected: DetectedPattern) {
        db.insights().insert(
            PatternInsightEntity(
                windowStart = Dates.iso(Dates.today().minusDays(13)),
                claim = detected.claim,
                evidenceJson = JSONArray(detected.evidence).toString(),
                failureMode = detected.failureMode,
                proposedTitle = detected.proposed.title,
                proposedDetail = detected.proposed.detail,
                proposedIfThen = detected.proposed.ifThen.orEmpty(),
                proposedQuota = detected.proposed.quota,
                proposedFrogLockHour = detected.proposed.frogLockHour,
                proposedKillStart = detected.proposed.killZoneStart,
                proposedKillEnd = detected.proposed.killZoneEnd,
                proposedNudgeHour = detected.proposed.nudgeHour,
                proposedNudgeMinute = detected.proposed.nudgeMinute,
            ),
        )
    }

    fun wasteLabel(pkg: String): String = SettingsStore.KNOWN_WASTE[pkg] ?: pkg.substringAfterLast('.')

    fun cardForReview(review: DayReviewEntity) = mentality.byId(review.mentalityId)

    fun failureCopy(mode: FailureMode): String = mentality.forMode(mode).line

    suspend fun seedDemoData() {
        DemoDataSeeder.seed(db, settings)
        ensureTodayPlan()
    }
}
