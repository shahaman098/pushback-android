package com.pushback.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.domain.MentalityCard
import com.pushback.app.domain.TodaySnapshot
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.HairlineRule
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.NewspaperClippingCard
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbField
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme

@Composable
fun EveningScreen(
    snap: TodaySnapshot?,
    card: MentalityCard?,
    onDone: (supposed: String, happened: String, well: String, wrong: String, tomorrow: String) -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var supposed by remember {
        mutableStateOf("Send ${snap?.quotaTarget ?: 5} applications. Slay Frog first. Complete compound leverage block.")
    }
    var happened by remember {
        mutableStateOf(
            if (snap != null) "Sent ${snap.sentToday} of ${snap.quotaTarget} applications. Compound block ${if (snap.compoundDone) "completed" else "not completed"}."
            else "",
        )
    }
    var well by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf("") }
    var tomorrow by remember { mutableStateOf(snap?.ifThen.orEmpty().ifBlank { "If it is 09:00, then I send 5 targeted applications before opening anything else." }) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Masthead("EVENING AFTER-ACTION REVIEW", "INSPECT • DIAGNOSE • RECALIBRATE")
        Spacer(Modifier.height(8.dp))
        Text("Inspect The Day", style = typography.title)
        Text(
            "What was supposed to happen. What actually happened. Why. Tomorrow's protocol. Not an emotional diary — instrument panel calibration.",
            style = typography.bodySmall,
            color = colors.muted,
        )
        Spacer(Modifier.height(14.dp))

        // Daily Scoreboard Card
        PaperSheet(borderColor = if (snap?.requiredRemaining == 0) colors.good else colors.debt) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Eyebrow("TODAY'S SCOREBOARD", if (snap?.requiredRemaining == 0) colors.good else colors.debt)
                TrackChip(
                    label = if (snap?.requiredRemaining == 0) "QUOTA MET" else "QUOTA DEFICIT",
                    color = if (snap?.requiredRemaining == 0) colors.good else colors.debt,
                    filled = true,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Applications Sent: ${snap?.sentToday ?: 0} / ${snap?.quotaTarget ?: 0} • Compound Block: ${if (snap?.compoundDone == true) "Done" else "Missed"}",
                style = typography.monoBold.copy(fontSize = 12.sp),
                color = colors.paper,
            )
        }

        Spacer(Modifier.height(14.dp))

        // Plan vs Reality Contrast Table
        PaperSheet {
            Eyebrow("1. SUPPOSED TO HAPPEN", colors.gold)
            Spacer(Modifier.height(6.dp))
            PbField(supposed, { supposed = it }, "The morning commitment.", minLines = 2)

            Spacer(Modifier.height(12.dp))
            Eyebrow("2. WHAT ACTUALLY HAPPENED", colors.gold)
            Spacer(Modifier.height(6.dp))
            PbField(happened, { happened = it }, "Objective reality. No spin.", minLines = 2)
        }

        Spacer(Modifier.height(14.dp))

        // Went Well vs Went Wrong
        PaperSheet {
            Eyebrow("3. WHAT WENT WELL", colors.good)
            Spacer(Modifier.height(6.dp))
            PbField(well, { well = it }, "What worked well? Keep doing this.", minLines = 2)

            Spacer(Modifier.height(12.dp))
            Eyebrow("4. WHAT WENT WRONG / IDENTIFIED WASTE", colors.debt)
            Spacer(Modifier.height(6.dp))
            PbField(wrong, { wrong = it }, "Name the distraction or delay. It won't name itself.", minLines = 2)
        }

        Spacer(Modifier.height(14.dp))

        // Tomorrow's If-Then Intention
        PaperSheet(borderColor = colors.gold) {
            Eyebrow("5. TOMORROW'S IF-THEN INTENTION", colors.gold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Lock in tomorrow's cue and start action before going to sleep.",
                style = typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.height(6.dp))
            PbField(tomorrow, { tomorrow = it }, "If [cue/time], then I [action].", minLines = 2)
        }

        // Associated Mentality Card
        if (card != null) {
            Spacer(Modifier.height(14.dp))
            NewspaperClippingCard(
                quote = card.line,
                author = "${card.thinker} (${card.failureMode.name.replace('_', ' ')})",
                category = "RECOMMENDED DISPATCH",
            )
        }

        Spacer(Modifier.height(18.dp))

        PbButton("Close The Edition & Save AAR ★", Modifier.fillMaxWidth()) {
            feedback.stampSlam()
            onDone(supposed, happened, well, wrong, tomorrow)
        }

        Spacer(Modifier.height(80.dp))
    }
}
