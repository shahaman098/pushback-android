package com.pushback.app.domain

import java.time.LocalDate
import java.time.LocalDateTime

enum class JobStatus {
    TO_APPLY, APPLIED, FOLLOWED_UP, INTERVIEW, OFFER, REJECTED
}

enum class TaskTrack { REQUIRED, COMPOUND, OPTIONAL }

enum class FailureMode { AVOIDANCE, SCROLLING, FAKE_WORK, NO_SEND, NO_REVIEW }

enum class EventType {
    QUOTA_HIT,
    POSTPONE,
    NOTIF_DISMISSED,
    MORNING_SKIP,
    MORNING_DONE,
    EVENING_DONE,
    WASTE_INTERRUPT,
    SEND,
    FOCUS_START,
    FOCUS_END,
}

data class MentalityCard(
    val id: String,
    val thinker: String,
    val line: String,
    val failureMode: FailureMode,
)

data class Woop(
    val wish: String = "",
    val outcome: String = "",
    val obstacle: String = "",
    val plan: String = "",
)

data class TodaySnapshot(
    val date: LocalDate,
    val identityLine: String,
    val outcome: String,
    val quotaTarget: Int,
    val sentToday: Int,
    val requiredRemaining: Int,
    val frogTitle: String?,
    val frogDone: Boolean,
    val compoundLocked: Boolean,
    val compoundTitle: String?,
    val compoundDone: Boolean,
    val consistency: Float,
    val daysHit: Int,
    val daysTracked: Int,
    val debtCount: Int,
    val morningDone: Boolean,
    val eveningDone: Boolean,
    val ifThen: String,
    val patternLine: String?,
    val nextNotificationLabel: String,
    val watchdogOn: Boolean,
    val usageGranted: Boolean,
    val now: LocalDateTime,
    val nextJobId: Long? = null,
    val nextJobCompany: String? = null,
    val nextJobRole: String? = null,
    val nextJobUrl: String? = null,
    val waitingToApply: Int = 0,
)

data class ProtocolChange(
    val title: String,
    val detail: String,
    val ifThen: String? = null,
    val quota: Int? = null,
    val frogLockHour: Int? = null,
    val killZoneStart: Int? = null,
    val killZoneEnd: Int? = null,
    val nudgeHour: Int? = null,
    val nudgeMinute: Int? = null,
)

data class DetectedPattern(
    val claim: String,
    val evidence: List<String>,
    val failureMode: FailureMode,
    val proposed: ProtocolChange,
)
