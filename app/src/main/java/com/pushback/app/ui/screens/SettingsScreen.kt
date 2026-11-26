package com.pushback.app.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.data.Settings
import com.pushback.app.data.SettingsStore
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.HairlineRule
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbField
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settings: Settings,
    usageGranted: Boolean,
    onUpdate: ((Settings) -> Settings) -> Unit,
    onSeedDemoData: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)
    val scope = rememberCoroutineScope()
    val notif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Masthead("SYSTEM CONFIGURATION", "THEMES • PROTOCOLS • WATCHDOG")
        Spacer(Modifier.height(8.dp))
        Text("Settings & Protocols", style = typography.title)
        Spacer(Modifier.height(14.dp))

        // Theme Mode Selector
        PaperSheet(borderColor = colors.gold) {
            Eyebrow("PRESS EDITION THEME", colors.gold)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "MORNING" to "Morning Gazette (Light)",
                    "MIDNIGHT" to "Midnight (Dark)",
                    "SEPIA" to "Sepia Monolith",
                ).forEach { (mode, label) ->
                    val isSelected = settings.themeMode.equals(mode, ignoreCase = true)
                    Box(
                        Modifier
                            .weight(1f)
                            .background(if (isSelected) colors.tape else colors.surfaceElevated)
                            .border(1.dp, if (isSelected) colors.night else colors.hairline)
                            .clickable {
                                feedback.click()
                                onUpdate { it.copy(themeMode = mode) }
                            }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            mode,
                            style = typography.labelSmall.copy(
                                color = if (isSelected) colors.tapeText else colors.paper,
                                fontSize = 9.5.sp,
                            ),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Demo Data Showcase Trigger
        PaperSheet {
            Eyebrow("DEMO SHOWCASE DATA", colors.gold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Populate 14 days of realistic job applications, WOOP briefings, AAR reviews, and behavioral pattern insights to showcase all features.",
                style = typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.height(10.dp))
            PbGhostButton("Seed 14-Day Sample History", Modifier.fillMaxWidth()) {
                feedback.success()
                onSeedDemoData?.invoke()
                Toast.makeText(context, "Loaded 14 days of sample history", Toast.LENGTH_SHORT).show()
            }
        }

        Spacer(Modifier.height(14.dp))

        // Goals & Identity
        PaperSheet {
            Eyebrow("12-MONTH NORTH STAR OUTCOME", colors.gold)
            Spacer(Modifier.height(6.dp))
            PbField(settings.outcome, { v -> onUpdate { it.copy(outcome = v) } }, "What this year is for (Role, product, runway)")

            Spacer(Modifier.height(12.dp))
            Eyebrow("OPERATING IDENTITY LINE", colors.gold)
            Spacer(Modifier.height(6.dp))
            PbField(settings.identityLine, { v -> onUpdate { it.copy(identityLine = v) } }, "I am a person who…")
        }

        Spacer(Modifier.height(14.dp))

        // Daily Targets & Sliders
        PaperSheet {
            Eyebrow("DAILY APPLICATION QUOTA: ${settings.quota}", colors.debt)
            Spacer(Modifier.height(4.dp))
            Slider(
                value = settings.quota.toFloat(),
                onValueChange = { v ->
                    feedback.tick()
                    onUpdate { it.copy(quota = v.toInt().coerceIn(it.quotaFloor, it.quotaCeiling)) }
                },
                valueRange = settings.quotaFloor.toFloat()..settings.quotaCeiling.toFloat(),
                steps = (settings.quotaCeiling - settings.quotaFloor - 1).coerceAtLeast(0),
                colors = SliderDefaults.colors(thumbColor = colors.debt, activeTrackColor = colors.debt, inactiveTrackColor = colors.muted),
            )

            Spacer(Modifier.height(10.dp))
            Eyebrow("COMPOUND BLOCK DURATION: ${settings.compoundMinutes}m", colors.gold)
            Spacer(Modifier.height(4.dp))
            Slider(
                value = settings.compoundMinutes.toFloat(),
                onValueChange = { v ->
                    feedback.tick()
                    onUpdate { it.copy(compoundMinutes = v.toInt().coerceIn(30, 180)) }
                },
                valueRange = 30f..180f,
                steps = 9,
                colors = SliderDefaults.colors(thumbColor = colors.gold, activeTrackColor = colors.gold, inactiveTrackColor = colors.muted),
            )
        }

        Spacer(Modifier.height(14.dp))

        // Notification Schedule
        PaperSheet {
            Eyebrow("PROTOCOL SCHEDULE TIMERS", colors.gold)
            Spacer(Modifier.height(8.dp))
            TimeRow("Morning Briefing WOOP", settings.wakeHour, settings.wakeMinute) { h, m ->
                feedback.click()
                onUpdate { it.copy(wakeHour = h, wakeMinute = m) }
            }
            HairlineRule()
            TimeRow("Midday Quota Nudge", settings.nudgeHour, settings.nudgeMinute) { h, m ->
                feedback.click()
                onUpdate { it.copy(nudgeHour = h, nudgeMinute = m) }
            }
            HairlineRule()
            TimeRow("Evening After-Action Review", settings.eveningHour, settings.eveningMinute) { h, m ->
                feedback.click()
                onUpdate { it.copy(eveningHour = h, eveningMinute = m) }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Features & Adaptation
        PaperSheet {
            Eyebrow("BEHAVIORAL WATCHDOG & ADAPTATION", colors.gold)
            Spacer(Modifier.height(8.dp))
            ToggleRow("Watchdog Shield during Focus", settings.watchdogEnabled) {
                feedback.click()
                onUpdate { s -> s.copy(watchdogEnabled = it) }
            }
            HairlineRule()
            ToggleRow("Pattern Adaptation Engine", settings.patternAdaptation) {
                feedback.click()
                onUpdate { s -> s.copy(patternAdaptation = it) }
            }
            HairlineRule()
            ToggleRow("Tactile Haptics", settings.hapticsEnabled) {
                feedback.click()
                onUpdate { s -> s.copy(hapticsEnabled = it) }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Permissions
        PaperSheet {
            Eyebrow("ANDROID PERMISSIONS", if (usageGranted) colors.good else colors.debt)
            Spacer(Modifier.height(6.dp))
            Text(
                if (usageGranted) "✓ Usage Access granted. Watchdog is fully functional."
                else "✗ Usage Access disabled. PushBack cannot intercept social media during focus.",
                style = typography.mono.copy(fontSize = 11.5.sp),
                color = if (usageGranted) colors.good else colors.debt,
            )
            Spacer(Modifier.height(10.dp))
            PbGhostButton("Open Usage Access Settings", Modifier.fillMaxWidth()) {
                context.startActivity(Intent(AndroidSettings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            Spacer(Modifier.height(8.dp))
            PbGhostButton("Allow Notifications", Modifier.fillMaxWidth()) {
                if (Build.VERSION.SDK_INT >= 33) notif.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            Spacer(Modifier.height(8.dp))
            PbGhostButton("Allow Exact Alarms", Modifier.fillMaxWidth()) {
                if (Build.VERSION.SDK_INT >= 31) {
                    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!am.canScheduleExactAlarms()) {
                        context.startActivity(Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Distraction Enemy App List
        PaperSheet {
            Eyebrow("DISTRACTION ENEMY APPS", colors.debt)
            Spacer(Modifier.height(6.dp))
            Text("These apps will be intercepted by the Watchdog whenever quota is open.", style = typography.bodySmall, color = colors.muted)
            Spacer(Modifier.height(10.dp))
            SettingsStore.KNOWN_WASTE.forEach { (pkg, label) ->
                val on = pkg in settings.wastePackages
                ToggleRow(label, on) { checked ->
                    feedback.click()
                    onUpdate { s ->
                        s.copy(
                            wastePackages = if (checked) s.wastePackages + pkg else s.wastePackages - pkg,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = typography.body, color = colors.paper, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.tape,
                checkedTrackColor = colors.night,
                uncheckedThumbColor = colors.muted,
                uncheckedTrackColor = colors.hairline,
            ),
        )
    }
}

@Composable
private fun TimeRow(label: String, hour: Int, minute: Int, onChange: (Int, Int) -> Unit) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = typography.body, color = colors.paper, modifier = Modifier.weight(1f))
        Text(
            "−",
            style = typography.title,
            color = colors.muted,
            modifier = Modifier
                .clickable {
                    val total = (hour * 60 + minute - 15).let { if (it < 0) it + 24 * 60 else it }
                    onChange(total / 60, total % 60)
                }
                .padding(8.dp),
        )
        Text("%02d:%02d".format(hour, minute), style = typography.monoBold, color = colors.gold)
        Text(
            "+",
            style = typography.title,
            color = colors.muted,
            modifier = Modifier
                .clickable {
                    val total = (hour * 60 + minute + 15) % (24 * 60)
                    onChange(total / 60, total % 60)
                }
                .padding(8.dp),
        )
    }
}
