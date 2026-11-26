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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.data.PatternInsightEntity
import com.pushback.app.data.Settings
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.HairlineRule
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.components.StatCell
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme
import org.json.JSONArray
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Composable
fun PatternsScreen(
    settings: Settings,
    pending: PatternInsightEntity?,
    history: List<PatternInsightEntity>,
    onAccept: (PatternInsightEntity) -> Unit,
    onReject: (PatternInsightEntity) -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    val days = if (settings.firstOpenAt == 0L) 0
    else TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - settings.firstOpenAt)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Masthead("PATTERN ENGINE", "ON-DEVICE INTELLIGENCE • NO CLOUD")
        Spacer(Modifier.height(8.dp))
        Text("Behavioral Patterns", style = typography.title)
        Text(
            if (days < 7) {
                "Days 1–7 are baseline observation. PushBack tracks send velocity, peak windows, and distraction drift. Then the protocol begins adapting."
            } else {
                "Every claim is a mathematical fact from your local SQLite database. If the engine cannot present hard evidence, it remains silent."
            },
            style = typography.bodySmall,
            color = colors.muted,
        )
        Spacer(Modifier.height(14.dp))

        // Engine Status Panel
        PaperSheet {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatCell("OBSERVATION DAY", "Day ${days + 1}", colors.gold)
                StatCell("ADAPTATION", if (settings.patternAdaptation) "ACTIVE" else "OFF", if (settings.patternAdaptation) colors.good else colors.muted)
                StatCell("MUTATIONS FILED", "${history.size}", colors.paper)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Pending Proposed Mutation
        if (pending != null) {
            PaperSheet(borderColor = colors.gold) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow("PROPOSED PROTOCOL MUTATION", colors.gold)
                    TrackChip("ACTION REQUIRED", colors.gold, filled = true)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    pending.claim,
                    style = typography.title.copy(fontSize = 22.sp, lineHeight = 26.sp),
                    color = colors.paper,
                )
                Spacer(Modifier.height(8.dp))
                Text(pending.proposedTitle, style = typography.title.copy(fontSize = 18.sp), color = colors.gold)
                Spacer(Modifier.height(4.dp))
                Text(pending.proposedDetail, style = typography.bodySmall, color = colors.muted)
                Spacer(Modifier.height(12.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.hairline)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("RECORDED EVIDENCE:", style = typography.labelSmall, color = colors.gold)
                        Spacer(Modifier.height(4.dp))
                        evidence(pending.evidenceJson).forEach { ev ->
                            Text("• $ev", style = typography.mono.copy(fontSize = 11.sp), color = colors.paper)
                        }
                    }
                }

                if (pending.proposedIfThen.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("PROPOSED IF-THEN:", style = typography.labelSmall, color = colors.gold)
                    Text(pending.proposedIfThen, style = typography.monoBold.copy(fontSize = 11.5.sp), color = colors.gold)
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PbButton("Accept Mutation", Modifier.weight(1f), color = colors.gold) {
                        feedback.stampSlam()
                        onAccept(pending)
                    }
                    PbGhostButton("Reject", Modifier.weight(1f)) {
                        feedback.warning()
                        onReject(pending)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        } else {
            PaperSheet {
                Eyebrow("PROTOCOL STATUS", colors.good)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (days < 7)
                        "Engine in calibration mode. Continue logging daily sends and after-action reviews."
                    else
                        "No pending mutations. Either your protocol is optimized, or this week's adaptation has already been applied.",
                    style = typography.body,
                    color = colors.paper,
                )
            }
            Spacer(Modifier.height(18.dp))
        }

        HairlineRule()
        Spacer(Modifier.height(12.dp))

        // Mutation History
        Eyebrow("MUTATION HISTORY LEDGER")
        Spacer(Modifier.height(8.dp))

        if (history.isEmpty()) {
            Text("No prior protocol mutations filed yet.", style = typography.mono, color = colors.muted)
        } else {
            history.forEach { item ->
                val dateStr = remember(item.createdAt) {
                    val dt = Instant.ofEpochMilli(item.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                    dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                }
                PaperSheet(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(dateStr, style = typography.mono.copy(fontSize = 10.5.sp), color = colors.muted)
                        TrackChip(
                            label = when {
                                item.acceptedAt != null -> "ACCEPTED"
                                item.rejectedAt != null -> "REJECTED"
                                else -> "PENDING"
                            },
                            color = when {
                                item.acceptedAt != null -> colors.good
                                item.rejectedAt != null -> colors.debt
                                else -> colors.gold
                            },
                            filled = true,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(item.claim, style = typography.bodySmall, color = colors.paper)
                    Spacer(Modifier.height(2.dp))
                    Text("Proposal: ${item.proposedTitle}", style = typography.monoBold.copy(fontSize = 11.sp), color = colors.gold)
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

private fun evidence(json: String): List<String> = try {
    val a = JSONArray(json)
    List(a.length()) { a.getString(it) }
} catch (_: Exception) {
    emptyList()
}
