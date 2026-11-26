package com.pushback.app.ui.screens

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
import com.pushback.app.data.DayPlanEntity
import com.pushback.app.data.Settings
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

@Composable
fun MorningScreen(
    settings: Settings,
    plan: DayPlanEntity?,
    patternLine: String?,
    onDone: (wish: String, outcome: String, obstacle: String, plan: String, ifThen: String, frog: String, identity: String) -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var identity by remember { mutableStateOf(plan?.identityLine?.ifBlank { settings.identityLine } ?: settings.identityLine) }
    var wish by remember { mutableStateOf(plan?.wish.orEmpty().ifBlank { "Send ${settings.quota} targeted applications before noon." }) }
    var outcome by remember { mutableStateOf(plan?.outcome.orEmpty().ifBlank { "Clear runway, move pipelines forward, unlock uninterrupted compound block." }) }
    var obstacle by remember { mutableStateOf(plan?.obstacle.orEmpty().ifBlank { "Drifting into social media feeds or polishing resume tabs instead of sending." }) }
    var woopPlan by remember { mutableStateOf(plan?.plan.orEmpty().ifBlank { "If I feel resistance, I will open PushBack and send listing #1 immediately." }) }
    var frog by remember { mutableStateOf("Send ${settings.quota} targeted applications") }
    var ifThen by remember {
        mutableStateOf(
            plan?.ifThen?.ifBlank {
                "If it is ${"%02d:%02d".format(settings.wakeHour, settings.wakeMinute)}, then I send the first application before I open anything else."
            } ?: "If it is 09:00, then I send 5 applications before anything else.",
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Masthead("MORNING DISPATCH", "WOOP • FROG • IF-THEN")
        Spacer(Modifier.height(8.dp))
        Text("Morning Briefing", style = typography.title)
        Text(
            "Two minutes of mental contrasting beat an hour of vague motivation. Name the wish, the obstacle, and the exact if-then execution plan.",
            style = typography.bodySmall,
            color = colors.muted,
        )

        if (!patternLine.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            PaperSheet(borderColor = colors.gold) {
                Eyebrow("FROM YOUR RECENT PATTERNS", colors.gold)
                Spacer(Modifier.height(4.dp))
                Text(patternLine, style = typography.bodySmall, color = colors.paper)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Step 1: Identity
        PaperSheet {
            Eyebrow("1. OPERATING IDENTITY", colors.gold)
            Spacer(Modifier.height(8.dp))
            PbField(identity, { identity = it }, "I am a person who…")
        }

        Spacer(Modifier.height(12.dp))

        // Step 2: The Frog
        PaperSheet(borderColor = colors.debt) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Eyebrow("2. THE DAILY FROG", colors.debt)
                TrackChip("PRIORITY #1", colors.debt, filled = true)
            }
            Spacer(Modifier.height(8.dp))
            Text("The single most critical task that must get done today.", style = typography.bodySmall, color = colors.muted)
            Spacer(Modifier.height(8.dp))
            PbField(frog, { frog = it }, "The Frog task title")
        }

        Spacer(Modifier.height(12.dp))

        // Step 3: WOOP Protocol
        PaperSheet {
            Eyebrow("3. WOOP MENTAL CONTRASTING", colors.gold)
            Spacer(Modifier.height(8.dp))

            Text("WISH (What do you specifically want from today?):", style = typography.labelSmall, color = colors.gold)
            PbField(wish, { wish = it }, "The Wish", minLines = 2)

            Spacer(Modifier.height(10.dp))
            Text("OUTCOME (Best result if you actually do it):", style = typography.labelSmall, color = colors.gold)
            PbField(outcome, { outcome = it }, "The Outcome", minLines = 2)

            Spacer(Modifier.height(10.dp))
            Text("OBSTACLE (The inner obstacle/excuse. Be brutally honest):", style = typography.labelSmall, color = colors.debt)
            PbField(obstacle, { obstacle = it }, "The Inner Obstacle", minLines = 2)

            Spacer(Modifier.height(10.dp))
            Text("PLAN (If [obstacle], then I will [action]):", style = typography.labelSmall, color = colors.good)
            PbField(woopPlan, { woopPlan = it }, "The Antidote Plan", minLines = 2)
        }

        Spacer(Modifier.height(12.dp))

        // Step 4: Implementation Intention (If-Then)
        PaperSheet(borderColor = colors.gold) {
            Eyebrow("4. IMPLEMENTATION INTENTION (IF-THEN)", colors.gold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Trigger cue -> Immediate physical action. Lock in the time and cue.",
                style = typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.height(8.dp))
            PbField(ifThen, { ifThen = it }, "If [cue/time], then [action]", minLines = 2)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Quota today: ${settings.quota} applications. Compound block: ${settings.compoundMinutes} minutes (unlocked only upon quota completion).",
            style = typography.mono.copy(fontSize = 11.5.sp),
            color = colors.muted,
        )

        Spacer(Modifier.height(16.dp))

        PbButton("Lock The Morning Plan ★", Modifier.fillMaxWidth()) {
            feedback.stampSlam()
            onDone(wish, outcome, obstacle, woopPlan, ifThen, frog, identity)
        }

        Spacer(Modifier.height(80.dp))
    }
}
