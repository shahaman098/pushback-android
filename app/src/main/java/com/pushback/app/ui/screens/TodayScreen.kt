package com.pushback.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.data.TaskEntity
import com.pushback.app.domain.JobParser
import com.pushback.app.domain.TaskTrack
import com.pushback.app.domain.TodaySnapshot
import com.pushback.app.ui.components.DoubleHairlineRule
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.HairlineRule
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.NewspaperClippingCard
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbField
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.components.StampDigit
import com.pushback.app.ui.components.TickerTapeBanner
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    snap: TodaySnapshot?,
    requiredTasks: List<TaskEntity>,
    onAddJob: (String, String, String) -> Unit,
    onMarkSent: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onMorning: () -> Unit,
    onEvening: () -> Unit,
    onFocus: () -> Unit,
    onJobs: () -> Unit,
    onPatterns: () -> Unit,
) {
    val context = LocalContext.current
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var showQuickAdd by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        if (snap == null) {
            Text("Setting the type...", style = typography.body, color = colors.muted)
            return
        }

        val daysSince = (snap.daysTracked.coerceAtLeast(1))
        val issueStr = "NO. ${42 + daysSince}"
        val volStr = "VOL. I"

        Masthead(
            kicker = if (snap.requiredRemaining > 0) "UNFINISHED EDITION • ${snap.requiredRemaining} DUE" else "EDITION PUT TO BED • QUOTA CLEARED",
            date = snap.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
            volume = volStr,
            issue = issueStr,
        )

        Spacer(Modifier.height(10.dp))

        // Dynamic ticker tape
        TickerTapeBanner(
            text = if (snap.requiredRemaining > 0)
                "QUOTA: ${snap.sentToday}/${snap.quotaTarget} SENT • ${(snap.consistency * 100).toInt()}% WEEKLY CONSISTENCY • PARKSON'S LAW IN EFFECT"
            else
                "★ QUOTA HIT: ${snap.sentToday} APPLICATIONS SENT • COMPOUND BLOCK UNLOCKED • BUILD LEVERAGE"
        )

        Spacer(Modifier.height(14.dp))

        // Lead Story Card: The Work / Target Job
        if (snap.requiredRemaining > 0) {
            PaperSheet(borderColor = colors.debt) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow("THE LEAD STORY • PRIORITY ACTION", colors.debt)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "${snap.requiredRemaining} of ${snap.quotaTarget} applications still required today.",
                    style = typography.title.copy(fontSize = 24.sp, lineHeight = 28.sp),
                    color = colors.paper,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Planning is not applying. Browsing is not a send. Move the needle.",
                    style = typography.bodySmall,
                    color = colors.muted,
                )
                Spacer(Modifier.height(14.dp))

                if (snap.nextJobId == null) {
                    Text(
                        "No listing currently queued. Name one now, or paste a link.",
                        style = typography.body,
                        color = colors.muted,
                    )
                    Spacer(Modifier.height(10.dp))
                    PbField(
                        value = url,
                        onValueChange = { input ->
                            url = input
                            if (company.isBlank()) {
                                val parsed = JobParser.parse(input)
                                if (parsed.company.isNotBlank()) company = parsed.company
                                if (parsed.role.isNotBlank() && role.isBlank()) role = parsed.role
                            }
                        },
                        hint = "Paste Job URL (auto-detects company & role)",
                    )
                    Spacer(Modifier.height(8.dp))
                    PbField(company, { company = it }, "Company Name")
                    Spacer(Modifier.height(8.dp))
                    PbField(role, { role = it }, "Target Role")
                    Spacer(Modifier.height(12.dp))
                    PbButton(
                        "Place on front page & apply",
                        Modifier.fillMaxWidth(),
                    ) {
                        if (company.isNotBlank() && role.isNotBlank()) {
                            feedback.stampSlam()
                            onAddJob(company.trim(), role.trim(), url.trim())
                            company = ""; role = ""; url = ""
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.hairline)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    snap.nextJobCompany.orEmpty().uppercase(),
                                    style = typography.label.copy(fontSize = 11.sp, color = colors.gold),
                                )
                                TrackChip("NEXT IN LINE", colors.gold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                snap.nextJobRole.orEmpty(),
                                style = typography.title.copy(fontSize = 24.sp, lineHeight = 28.sp),
                                color = colors.paper,
                            )
                            if (!snap.nextJobUrl.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    snap.nextJobUrl,
                                    style = typography.mono.copy(fontSize = 11.sp),
                                    color = colors.muted,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PbButton(
                            text = "Open & Apply",
                            modifier = Modifier.weight(1.2f),
                        ) {
                            val link = snap.nextJobUrl.orEmpty()
                            if (link.isNotBlank()) {
                                val parsed = runCatching {
                                    val raw = if (link.startsWith("http")) link else "https://$link"
                                    Uri.parse(raw)
                                }.getOrNull()
                                if (parsed != null) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, parsed).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }
                            } else {
                                onJobs()
                            }
                        }
                        PbButton(
                            text = "Mark Sent ✓",
                            color = colors.gold,
                            modifier = Modifier.weight(1.2f),
                        ) {
                            feedback.stampSlam()
                            onMarkSent(snap.nextJobId)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    PbGhostButton("Skip to next listing in queue", Modifier.fillMaxWidth()) {
                        onSkip(snap.nextJobId)
                    }
                    if (snap.waitingToApply > 1) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${snap.waitingToApply} listings in queue. Oldest first. Clear them sequentially.",
                            style = typography.mono.copy(fontSize = 10.5.sp),
                            color = colors.muted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        } else {
            // Quota Cleared Trophy Banner
            PaperSheet(borderColor = colors.good) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow("QUOTA HONORED ★", colors.good)
                    TrackChip("COMPLETED", colors.good, filled = true)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "All ${snap.quotaTarget} applications submitted.",
                    style = typography.title.copy(fontSize = 24.sp),
                    color = colors.paper,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Runway work is done for today. Leverage & compound block are now active.",
                    style = typography.bodySmall,
                    color = colors.muted,
                )
                Spacer(Modifier.height(12.dp))
                if (!snap.eveningDone) {
                    PbButton(
                        "File After-Action Review",
                        Modifier.fillMaxWidth(),
                        color = colors.gold,
                        onClick = onEvening,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // The Instrument Dial & Stats Section
        PaperSheet {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StampDigit(
                    value = snap.requiredRemaining.toString(),
                    label = if (snap.requiredRemaining > 0) "still due" else "cleared",
                    hot = snap.requiredRemaining > 0,
                    modifier = Modifier.size(160.dp),
                    onClick = { feedback.stampSlam() },
                )
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        "${snap.sentToday} sent today",
                        style = typography.title.copy(fontSize = 20.sp),
                        color = colors.paper,
                    )
                    Text(
                        "Weekly: ${(snap.consistency * 100).toInt()}% · Debt: ${snap.debtCount}",
                        style = typography.monoBold.copy(fontSize = 11.sp),
                        color = if (snap.debtCount > 0) colors.debt else colors.muted,
                    )
                    Spacer(Modifier.height(10.dp))
                    PbGhostButton("Focus Timer", Modifier.fillMaxWidth(), onClick = onFocus)
                    Spacer(Modifier.height(6.dp))
                    PbGhostButton("Pipeline (${snap.waitingToApply})", Modifier.fillMaxWidth(), onClick = onJobs)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Required Tasks & Daily Frog
        val openRequired = requiredTasks.filter { it.track == TaskTrack.REQUIRED }
        if (openRequired.isNotEmpty()) {
            PaperSheet {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow("REQUIRED PROTOCOL TASKS")
                    Text("${openRequired.count { it.doneAt != null }}/${openRequired.size} DONE", style = typography.labelSmall, color = colors.muted)
                }
                Spacer(Modifier.height(8.dp))
                openRequired.forEach { task ->
                    val isDone = task.doneAt != null
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                feedback.click()
                                onToggleTask(task)
                            }
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrackChip(
                            label = if (isDone) "DONE" else if (task.isFrog) "FROG" else "DUE",
                            color = if (isDone) colors.good else if (task.isFrog) colors.debt else colors.gold,
                            filled = isDone || task.isFrog,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            task.title,
                            style = typography.body.copy(
                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                            ),
                            color = if (isDone) colors.muted else colors.paper,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    HairlineRule()
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // Compound Block Section
        PaperSheet(borderColor = if (snap.compoundLocked) colors.hairline else colors.gold) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Eyebrow("COMPOUND BLOCK", colors.gold)
                TrackChip(
                    label = if (snap.compoundLocked) "LOCKED" else "UNLOCKED",
                    color = if (snap.compoundLocked) colors.muted else colors.gold,
                    filled = !snap.compoundLocked,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (snap.compoundLocked)
                    "Locked. Hit the daily quota first. Ownership waits until runway is moving."
                else
                    (snap.compoundTitle ?: "Build leverage: write code, ship products, create media."),
                style = typography.title.copy(fontSize = 20.sp),
                color = if (snap.compoundLocked) colors.muted else colors.paper,
            )
        }

        // Pattern Insight banner if available
        if (!snap.patternLine.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            PaperSheet(borderColor = colors.gold) {
                Eyebrow("LOG PATTERN DETECTED", colors.gold)
                Spacer(Modifier.height(8.dp))
                Text(snap.patternLine, style = typography.body, color = colors.paper)
                Spacer(Modifier.height(10.dp))
                PbGhostButton("Inspect the evidence", Modifier.fillMaxWidth(), onClick = onPatterns)
            }
        }

        Spacer(Modifier.height(14.dp))

        // Daily Mentality Dispatch
        NewspaperClippingCard(
            quote = snap.identityLine,
            author = "OPERATING IDENTITY",
            category = "THE PRESS DISPATCH",
            onClick = onMorning,
        )

        Spacer(Modifier.height(16.dp))

        // Quick Briefing and Review actions
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PbGhostButton(
                text = if (snap.morningDone) "Morning WOOP ✓" else "Morning WOOP",
                modifier = Modifier.weight(1f),
                onClick = onMorning,
            )
            PbGhostButton(
                text = if (snap.eveningDone) "Evening AAR ✓" else "Evening AAR",
                modifier = Modifier.weight(1f),
                onClick = onEvening,
            )
        }

        Spacer(Modifier.height(16.dp))
        DoubleHairlineRule()
        Spacer(Modifier.height(10.dp))

        // Watchdog & Permission status footer
        Text(
            if (snap.usageGranted)
                "● Watchdog active while quota is open. Time-wasters will be intercepted."
            else
                "○ Usage access off. PushBack will nag, but cannot catch YouTube in the act.",
            style = typography.mono.copy(fontSize = 11.sp),
            color = if (snap.usageGranted) colors.gold else colors.muted,
        )

        Spacer(Modifier.height(80.dp))
    }
}
