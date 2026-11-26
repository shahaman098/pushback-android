package com.pushback.app.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.data.SettingsStore
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbField
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme

@Composable
fun OnboardingScreen(onFinish: (outcome: String, quota: Int, identity: String, ifThen: String, waste: Set<String>) -> Unit) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var step by remember { mutableIntStateOf(0) }
    var outcome by remember { mutableStateOf("") }
    var quota by remember { mutableIntStateOf(5) }
    var identity by remember { mutableStateOf("I am a person who sends the work, then builds leverage.") }
    var ifThen by remember { mutableStateOf("If it is 09:00, then I send five targeted applications before I open anything else.") }
    var waste by remember { mutableStateOf(SettingsStore.DEFAULT_WASTE) }
    val context = LocalContext.current
    val notif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Masthead(kicker = "ISSUE 00 • SETTING THE PRESS", date = "FOUNDATION DISPATCH")
        Spacer(Modifier.height(14.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboardingSteps",
        ) { targetStep ->
            when (targetStep) {
                0 -> {
                    Column {
                        Text(
                            "Jobs buy runway.\nOwnership buys wealth.",
                            style = typography.titleLarge.copy(fontSize = 32.sp, lineHeight = 36.sp),
                            color = colors.paper,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "PushBack is not an encouraging cheerleader. It is an uncompromising daily broadsheet operating system. Required work first — applications, interviews, follow-ups. Then one compound block. Distraction gets named in print.",
                            style = typography.body,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(20.dp))
                        PaperSheet(borderColor = colors.gold) {
                            Eyebrow("TWELVE-MONTH PRIMARY OUTCOME", colors.gold)
                            Spacer(Modifier.height(8.dp))
                            PbField(
                                value = outcome,
                                onValueChange = { outcome = it },
                                hint = "A specific role, salary number, or product launch.",
                                minLines = 3,
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        PbButton("Set Outcome & Continue", Modifier.fillMaxWidth()) {
                            if (outcome.isNotBlank()) {
                                feedback.stampSlam()
                                step = 1
                            }
                        }
                    }
                }
                1 -> {
                    Column {
                        Text("Daily Application Quota", style = typography.title)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "How many targeted applications will you submit every single day? Too high and you burn out. Too low and you are hiding. Five is the standard.",
                            style = typography.bodySmall,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(16.dp))
                        PaperSheet(borderColor = colors.debt) {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    "$quota",
                                    style = typography.display.copy(color = colors.debt, fontSize = 80.sp, lineHeight = 76.sp),
                                )
                                Text("APPLICATIONS / DAY", style = typography.labelSmall, color = colors.debt)
                            }
                            Spacer(Modifier.height(14.dp))
                            Slider(
                                value = quota.toFloat(),
                                onValueChange = {
                                    feedback.tick()
                                    quota = it.toInt().coerceIn(1, 12)
                                },
                                valueRange = 1f..12f,
                                steps = 10,
                                colors = SliderDefaults.colors(thumbColor = colors.debt, activeTrackColor = colors.debt, inactiveTrackColor = colors.muted),
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        PbButton("Lock Quota Target", Modifier.fillMaxWidth()) {
                            feedback.stampSlam()
                            step = 2
                        }
                    }
                }
                2 -> {
                    Column {
                        Text("Distraction Enemies", style = typography.title)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Select the apps that steal your runway. They will be intercepted by the Watchdog only while your daily quota remains unfulfilled.",
                            style = typography.bodySmall,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(14.dp))
                        SettingsStore.KNOWN_WASTE.forEach { (pkg, label) ->
                            val on = pkg in waste
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .background(if (on) colors.tape.copy(alpha = 0.25f) else colors.surface)
                                    .border(1.dp, if (on) colors.tape else colors.hairline)
                                    .clickable {
                                        feedback.click()
                                        waste = if (on) waste - pkg else waste + pkg
                                    }
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label, style = typography.body, color = colors.paper)
                                TrackChip(if (on) "GUARDED" else "IGNORE", if (on) colors.debt else colors.muted, filled = on)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        PbButton("Confirm Distraction Targets", Modifier.fillMaxWidth()) {
                            feedback.stampSlam()
                            step = 3
                        }
                    }
                }
                3 -> {
                    Column {
                        Text("Grant System Knives", style = typography.title)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Notifications and Usage Access empower PushBack to guard your focus. Without usage access, PushBack can still nag, but cannot catch YouTube in the act.",
                            style = typography.bodySmall,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(16.dp))
                        PbGhostButton("Allow Push Notifications", Modifier.fillMaxWidth()) {
                            feedback.click()
                            if (Build.VERSION.SDK_INT >= 33) notif.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        Spacer(Modifier.height(10.dp))
                        PbGhostButton("Grant Usage Access Permission", Modifier.fillMaxWidth()) {
                            feedback.click()
                            context.startActivity(Intent(AndroidSettings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                        Spacer(Modifier.height(24.dp))
                        PbButton("Continue", Modifier.fillMaxWidth()) {
                            feedback.stampSlam()
                            step = 4
                        }
                    }
                }
                else -> {
                    Column {
                        Text("Operating Identity & Cue", style = typography.title)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Implementation intentions beat motivation. Formulate your starting identity line and morning execution cue.",
                            style = typography.bodySmall,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(16.dp))
                        PaperSheet {
                            Eyebrow("OPERATING IDENTITY", colors.gold)
                            Spacer(Modifier.height(6.dp))
                            PbField(identity, { identity = it }, "I am a person who…")

                            Spacer(Modifier.height(12.dp))
                            Eyebrow("INITIAL IF-THEN INTENTION", colors.gold)
                            Spacer(Modifier.height(6.dp))
                            PbField(ifThen, { ifThen = it }, "If [cue/time], then I [action].", minLines = 2)
                        }
                        Spacer(Modifier.height(24.dp))
                        PbButton("Launch The Operating System ★", Modifier.fillMaxWidth()) {
                            feedback.stampSlam()
                            onFinish(outcome, quota, identity, ifThen, waste)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // Step Progress Indicator
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(5) { i ->
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .width(36.dp)
                        .height(6.dp)
                        .background(if (i <= step) colors.gold else colors.hairline.copy(alpha = 0.4f)),
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}
