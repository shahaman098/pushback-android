package com.pushback.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.data.FocusSessionEntity
import com.pushback.app.data.TaskEntity
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.HairlineRule
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.NewspaperClippingCard
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme
import com.pushback.app.usage.WatchdogService
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FocusScreen(
    snapRemaining: Int,
    frog: TaskEntity?,
    active: FocusSessionEntity?,
    compoundMinutes: Int,
    watchdogOn: Boolean,
    usageGranted: Boolean,
    onStart: (Long?, Int, (Long) -> Unit) -> Unit,
    onEnd: (Long) -> Unit,
) {
    val context = LocalContext.current
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var sessionId by remember { mutableLongStateOf(active?.id ?: 0L) }
    var remainingSec by remember { mutableIntStateOf(0) }
    var totalPlannedSec by remember { mutableIntStateOf(active?.plannedMinutes?.times(60) ?: 1500) }
    var ambientMode by remember { mutableStateOf(false) }

    val live = active != null || sessionId != 0L

    LaunchedEffect(active, sessionId) {
        val session = active
        if (session != null) {
            sessionId = session.id
            totalPlannedSec = (session.plannedMinutes * 60).coerceAtLeast(1)
            val end = session.startedAt + session.plannedMinutes * 60_000L
            while (true) {
                val rem = ((end - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
                remainingSec = rem
                if (remainingSec == 0) break
                delay(1000)
            }
        }
    }

    if (ambientMode && live) {
        // Fullscreen Minimalist Analog Ambient Mode
        Box(
            Modifier
                .fillMaxSize()
                .background(colors.ink)
                .clickable { ambientMode = false }
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("FOCUS MODE ACTIVE", style = typography.label, color = colors.gold)
                Spacer(Modifier.height(24.dp))
                CircularAnalogTimer(
                    remainingSec = remainingSec,
                    totalSec = totalPlannedSec,
                    modifier = Modifier.size(280.dp),
                )
                Spacer(Modifier.height(24.dp))
                Text(frog?.title ?: "Stay on the required output.", style = typography.body, color = colors.paper)
                Spacer(Modifier.height(8.dp))
                Text("Tap anywhere to return to controls", style = typography.labelSmall, color = colors.muted)
            }
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Masthead("PARKINSON'S TERMINAL", "TIMEBOX • EXECUTE • COMPLETE")
        Spacer(Modifier.height(8.dp))
        Text("Deep Focus Terminal", style = typography.title)
        Text(
            "Parkinson's Law: Applications and work expand to fill the available time. Timebox the session, slay the frog, and stop.",
            style = typography.bodySmall,
            color = colors.muted,
        )
        Spacer(Modifier.height(16.dp))

        if (live && remainingSec > 0) {
            // Live Focus Session Active
            PaperSheet(borderColor = colors.debt) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow("SESSION IN PROGRESS", colors.debt)
                    TrackChip("LIVE", colors.debt, filled = true)
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularAnalogTimer(
                        remainingSec = remainingSec,
                        totalSec = totalPlannedSec,
                        modifier = Modifier.size(220.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        frog?.title ?: "Required work block in progress.",
                        style = typography.title.copy(fontSize = 20.sp),
                        color = colors.paper,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "No phone browsing. No new tabs. Finish the active target.",
                        style = typography.bodySmall,
                        color = colors.muted,
                    )
                }

                Spacer(Modifier.height(16.dp))
                HairlineRule()
                Spacer(Modifier.height(12.dp))

                // Watchdog Status HUD
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        if (!usageGranted) "○ Watchdog: No usage permission"
                        else if (watchdogOn) "● Watchdog: Guarding focus"
                        else "○ Watchdog: Off",
                        style = typography.mono.copy(fontSize = 11.sp),
                        color = if (watchdogOn && usageGranted) colors.gold else colors.muted,
                    )
                    PbGhostButton("Ambient Mode", onClick = { ambientMode = true })
                }

                Spacer(Modifier.height(16.dp))

                PbButton(
                    text = "End Focus Session",
                    color = colors.night,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    feedback.stampSlam()
                    val id = active?.id ?: sessionId
                    if (id != 0L) onEnd(id)
                    WatchdogService.stop(context)
                    sessionId = 0
                }
            }
        } else {
            // Focus Setup / Presets
            PaperSheet {
                Eyebrow("THE TARGET")
                Spacer(Modifier.height(8.dp))
                Text(
                    if (snapRemaining > 0)
                        "$snapRemaining applications still required today."
                    else
                        "Daily quota is cleared! Compound leverage block is available.",
                    style = typography.title.copy(fontSize = 22.sp),
                    color = colors.paper,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    frog?.title ?: "No Frog designated in morning briefing.",
                    style = typography.bodySmall,
                    color = colors.muted,
                )
            }

            Spacer(Modifier.height(16.dp))

            Eyebrow("SELECT TIMEBOX PRESET", colors.gold)
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Preset 1: Standard 90m or Compound block
                PbButton(
                    text = if (snapRemaining > 0) "90m Frog Slay (Deep Work)" else "Compound Block (${compoundMinutes}m)",
                    color = if (snapRemaining > 0) colors.debt else colors.gold,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    feedback.stampSlam()
                    val minutes = if (snapRemaining > 0) 90 else compoundMinutes
                    totalPlannedSec = minutes * 60
                    onStart(frog?.id, minutes) { id ->
                        sessionId = id
                        if (watchdogOn) WatchdogService.start(context)
                    }
                }

                // Preset 2: 25m Sprint
                PbGhostButton(
                    text = "25-Minute Send Sprint (Pomodoro)",
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    feedback.click()
                    totalPlannedSec = 25 * 60
                    onStart(frog?.id, 25) { id ->
                        sessionId = id
                        if (watchdogOn) WatchdogService.start(context)
                    }
                }

                // Preset 3: 50m Focused Session
                PbGhostButton(
                    text = "50-Minute Single-Listing Focus",
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    feedback.click()
                    totalPlannedSec = 50 * 60
                    onStart(frog?.id, 50) { id ->
                        sessionId = id
                        if (watchdogOn) WatchdogService.start(context)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            NewspaperClippingCard(
                quote = "Parkinson's Law dictates that work expands to fill the time allotted for its completion. Compress the time, and output skyrockets.",
                author = "ANDY GROVE • HIGH OUTPUT MANAGEMENT",
                category = "MENTALITY",
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun CircularAnalogTimer(
    remainingSec: Int,
    totalSec: Int,
    modifier: Modifier = Modifier,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    val progress = (remainingSec.toFloat() / totalSec.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background Track
            drawCircle(
                color = colors.hairline.copy(alpha = 0.35f),
                radius = radius,
                style = Stroke(width = strokeWidth),
            )

            // Tick Marks
            val tickCount = 60
            for (i in 0 until tickCount) {
                val angle = (i * (360f / tickCount) - 90f) * (Math.PI / 180f)
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) 10.dp.toPx() else 4.dp.toPx()
                val startRadius = radius - strokeWidth / 2f - 4.dp.toPx()
                val endRadius = startRadius - tickLength

                val start = Offset(
                    (center.x + startRadius * cos(angle)).toFloat(),
                    (center.y + startRadius * sin(angle)).toFloat(),
                )
                val end = Offset(
                    (center.x + endRadius * cos(angle)).toFloat(),
                    (center.y + endRadius * sin(angle)).toFloat(),
                )

                drawLine(
                    color = if (isMajor) colors.gold else colors.muted.copy(alpha = 0.4f),
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                )
            }

            // Animated Progress Arc
            drawArc(
                color = colors.debt,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                formatClock(remainingSec),
                style = typography.display.copy(
                    fontSize = 48.sp,
                    lineHeight = 48.sp,
                    color = colors.paper,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "REMAINING",
                style = typography.labelSmall.copy(color = colors.gold),
            )
        }
    }
}

private fun formatClock(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}
