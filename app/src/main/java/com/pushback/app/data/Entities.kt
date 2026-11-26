package com.pushback.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pushback.app.domain.EventType
import com.pushback.app.domain.FailureMode
import com.pushback.app.domain.JobStatus
import com.pushback.app.domain.TaskTrack

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val company: String,
    val role: String,
    val url: String = "",
    val status: JobStatus = JobStatus.TO_APPLY,
    val createdAt: Long = System.currentTimeMillis(),
    val appliedAt: Long? = null,
    val notes: String = "",
)

@Entity(
    tableName = "tasks",
    indices = [Index("dueDate"), Index("track")],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val track: TaskTrack,
    val dueDate: String,
    val doneAt: Long? = null,
    val postponedReason: String? = null,
    val isFrog: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "day_plans", indices = [Index(value = ["date"], unique = true)])
data class DayPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val identityLine: String,
    val wish: String = "",
    val outcome: String = "",
    val obstacle: String = "",
    val plan: String = "",
    val frogTaskId: Long? = null,
    val quotaTarget: Int,
    val sentCount: Int = 0,
    val quotaHit: Boolean = false,
    val quotaHitAt: Long? = null,
    val ifThen: String = "",
    val morningDoneAt: Long? = null,
    val patternLine: String = "",
)

@Entity(tableName = "day_reviews", indices = [Index(value = ["date"], unique = true)])
data class DayReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val supposed: String,
    val happened: String,
    val wentWell: String,
    val wentWrong: String,
    val tomorrowIfThen: String,
    val mentalityId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val interruptedByWasteCount: Int = 0,
    val plannedMinutes: Int,
)

@Entity(tableName = "usage_slices")
data class UsageSliceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val minutes: Float,
    val requiredWasOpen: Boolean,
)

@Entity(tableName = "behavior_events")
data class BehaviorEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: EventType,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: String = "",
)

@Entity(tableName = "pattern_insights")
data class PatternInsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val windowStart: String,
    val claim: String,
    val evidenceJson: String,
    val failureMode: FailureMode,
    val proposedTitle: String,
    val proposedDetail: String,
    val proposedIfThen: String = "",
    val proposedQuota: Int? = null,
    val proposedFrogLockHour: Int? = null,
    val proposedKillStart: Int? = null,
    val proposedKillEnd: Int? = null,
    val proposedNudgeHour: Int? = null,
    val proposedNudgeMinute: Int? = null,
    val acceptedAt: Long? = null,
    val rejectedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
