package com.pushback.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore("pushback_settings")

data class Settings(
    val onboardingDone: Boolean = false,
    val outcome: String = "",
    val identityLine: String = "I am a person who sends the work, then builds leverage.",
    val quota: Int = 5,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 30,
    val eveningHour: Int = 21,
    val eveningMinute: Int = 0,
    val compoundMinutes: Int = 90,
    val watchdogEnabled: Boolean = true,
    val patternAdaptation: Boolean = true,
    val wastePackages: Set<String> = SettingsStore.DEFAULT_WASTE,
    val quotaFloor: Int = 1,
    val quotaCeiling: Int = 12,
    val frogLockHour: Int? = null,
    val killZoneStart: Int? = null,
    val killZoneEnd: Int? = null,
    val nudgeHour: Int = 13,
    val nudgeMinute: Int = 0,
    val allowedBreakUntil: Long = 0L,
    val firstOpenAt: Long = 0L,
    val themeMode: String = "MORNING",
    val hapticsEnabled: Boolean = true,
)

class SettingsStore(private val context: Context) {
    val flow: Flow<Settings> = context.settingsStore.data.map { it.toSettings() }

    suspend fun current(): Settings = flow.first()

    suspend fun update(transform: (Settings) -> Settings) {
        val next = transform(current())
        context.settingsStore.edit { p ->
            p[ONBOARDING] = next.onboardingDone
            p[OUTCOME] = next.outcome
            p[IDENTITY] = next.identityLine
            p[QUOTA] = next.quota
            p[WAKE_H] = next.wakeHour
            p[WAKE_M] = next.wakeMinute
            p[EVE_H] = next.eveningHour
            p[EVE_M] = next.eveningMinute
            p[COMPOUND] = next.compoundMinutes
            p[WATCHDOG] = next.watchdogEnabled
            p[ADAPT] = next.patternAdaptation
            p[WASTE] = next.wastePackages.joinToString(",")
            p[FLOOR] = next.quotaFloor
            p[CEIL] = next.quotaCeiling
            p[FROG_LOCK] = next.frogLockHour ?: -1
            p[KILL_S] = next.killZoneStart ?: -1
            p[KILL_E] = next.killZoneEnd ?: -1
            p[NUDGE_H] = next.nudgeHour
            p[NUDGE_M] = next.nudgeMinute
            p[BREAK_UNTIL] = next.allowedBreakUntil
            p[FIRST_OPEN] = next.firstOpenAt
            p[THEME_MODE] = next.themeMode
            p[HAPTICS_ENABLED] = next.hapticsEnabled
        }
    }

    private fun Preferences.toSettings() = Settings(
        onboardingDone = this[ONBOARDING] ?: false,
        outcome = this[OUTCOME] ?: "",
        identityLine = this[IDENTITY] ?: "I am a person who sends the work, then builds leverage.",
        quota = this[QUOTA] ?: 5,
        wakeHour = this[WAKE_H] ?: 7,
        wakeMinute = this[WAKE_M] ?: 30,
        eveningHour = this[EVE_H] ?: 21,
        eveningMinute = this[EVE_M] ?: 0,
        compoundMinutes = this[COMPOUND] ?: 90,
        watchdogEnabled = this[WATCHDOG] ?: true,
        patternAdaptation = this[ADAPT] ?: true,
        wastePackages = this[WASTE]?.split(",")?.filter { it.isNotBlank() }?.toSet()
            ?: DEFAULT_WASTE,
        quotaFloor = this[FLOOR] ?: 1,
        quotaCeiling = this[CEIL] ?: 12,
        frogLockHour = this[FROG_LOCK]?.takeIf { it >= 0 },
        killZoneStart = this[KILL_S]?.takeIf { it >= 0 },
        killZoneEnd = this[KILL_E]?.takeIf { it >= 0 },
        nudgeHour = this[NUDGE_H] ?: 13,
        nudgeMinute = this[NUDGE_M] ?: 0,
        allowedBreakUntil = this[BREAK_UNTIL] ?: 0L,
        firstOpenAt = this[FIRST_OPEN] ?: 0L,
        themeMode = this[THEME_MODE] ?: "MORNING",
        hapticsEnabled = this[HAPTICS_ENABLED] ?: true,
    )

    companion object {
        val DEFAULT_WASTE = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.netflix.mediaclient",
            "com.facebook.orca",
        )

        val KNOWN_WASTE = mapOf(
            "com.instagram.android" to "Instagram",
            "com.zhiliaoapp.musically" to "TikTok",
            "com.ss.android.ugc.trill" to "TikTok",
            "com.google.android.youtube" to "YouTube",
            "com.twitter.android" to "X",
            "com.reddit.frontpage" to "Reddit",
            "com.facebook.katana" to "Facebook",
            "com.snapchat.android" to "Snapchat",
            "com.netflix.mediaclient" to "Netflix",
            "com.facebook.orca" to "Messenger",
            "com.linkedin.android" to "LinkedIn",
        )

        private val ONBOARDING = booleanPreferencesKey("onboarding")
        private val OUTCOME = stringPreferencesKey("outcome")
        private val IDENTITY = stringPreferencesKey("identity")
        private val QUOTA = intPreferencesKey("quota")
        private val WAKE_H = intPreferencesKey("wake_h")
        private val WAKE_M = intPreferencesKey("wake_m")
        private val EVE_H = intPreferencesKey("eve_h")
        private val EVE_M = intPreferencesKey("eve_m")
        private val COMPOUND = intPreferencesKey("compound")
        private val WATCHDOG = booleanPreferencesKey("watchdog")
        private val ADAPT = booleanPreferencesKey("adapt")
        private val WASTE = stringPreferencesKey("waste")
        private val FLOOR = intPreferencesKey("floor")
        private val CEIL = intPreferencesKey("ceil")
        private val FROG_LOCK = intPreferencesKey("frog_lock")
        private val KILL_S = intPreferencesKey("kill_s")
        private val KILL_E = intPreferencesKey("kill_e")
        private val NUDGE_H = intPreferencesKey("nudge_h")
        private val NUDGE_M = intPreferencesKey("nudge_m")
        private val BREAK_UNTIL = longPreferencesKey("break_until")
        private val FIRST_OPEN = longPreferencesKey("first_open")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }
}
