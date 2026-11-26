package com.pushback.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.data.DayPlanEntity
import com.pushback.app.data.DayReviewEntity
import com.pushback.app.domain.Dates
import com.pushback.app.domain.MentalityCard
import com.pushback.app.ui.components.Eyebrow
import com.pushback.app.ui.components.HairlineRule
import com.pushback.app.ui.components.HeatmapGrid
import com.pushback.app.ui.components.Masthead
import com.pushback.app.ui.components.PaperSheet
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.components.StatCell
import com.pushback.app.ui.components.TrackChip
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.theme.PushBackTheme

@Composable
fun LogScreen(
    plans: List<DayPlanEntity>,
    reviews: List<DayReviewEntity>,
    cardFor: (DayReviewEntity) -> MentalityCard?,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val context = LocalContext.current
    val feedback = rememberTactileFeedback(true)

    val byDate = (plans.map { it.date } + reviews.map { it.date }).distinct().sortedDescending()
    val planMap = plans.associateBy { it.date }
    val reviewMap = reviews.associateBy { it.date }

    var selectedFilter by remember { mutableStateOf("ALL") }

    // Build 30-day heatmap data
    val today = Dates.today()
    val heatmapData = remember(plans) {
        (0 until 30).map { offset ->
            val d = today.minusDays(offset.toLong())
            val dIso = Dates.iso(d)
            val p = planMap[dIso]
            dIso to (p?.quotaHit)
        }.reversed()
    }

    val totalSends = plans.sumOf { it.sentCount }
    val hits = plans.count { it.quotaHit }
    val hitRate = if (plans.isNotEmpty()) (hits * 100 / plans.size) else 0

    val filteredDates = byDate.filter { date ->
        val plan = planMap[date]
        val review = reviewMap[date]
        when (selectedFilter) {
            "HITS" -> plan?.quotaHit == true
            "MISSED" -> plan != null && !plan.quotaHit
            "WITH AAR" -> review != null
            else -> true
        }
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        item {
            Masthead("INSTRUMENT LOG", "AFTER-ACTION ARCHIVE")
            Spacer(Modifier.height(8.dp))
            Text("Day Log Archive", style = typography.title)
            Text(
                "Swipe the broadsheet history with your thumb. Look for the weeks you lied to yourself.",
                style = typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.height(14.dp))

            // Stats Cards Row
            PaperSheet {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatCell("HIT RATE", "$hitRate%", if (hitRate >= 70) colors.good else colors.debt)
                    StatCell("TOTAL SENDS", "$totalSends", colors.gold)
                    StatCell("DAYS TRACKED", "${plans.size}", colors.paper)
                    StatCell("AAR REVIEWS", "${reviews.size}", colors.paper)
                }
            }

            Spacer(Modifier.height(14.dp))

            // 30-day Visual Heatmap
            PaperSheet {
                HeatmapGrid(dayHits = heatmapData)
            }

            Spacer(Modifier.height(14.dp))

            // Export Dispatch Action
            PbGhostButton(
                text = "Export Plaintext Report to Clipboard",
                modifier = Modifier.fillMaxWidth(),
            ) {
                feedback.success()
                val report = buildString {
                    appendLine("PUSHBACK BROAD SHEET ARCHIVE REPORT")
                    appendLine("Generated: ${Dates.todayIso()}")
                    appendLine("Total Sends: $totalSends | Hit Rate: $hitRate%")
                    appendLine("----------------------------------------")
                    byDate.take(14).forEach { d ->
                        val p = planMap[d]
                        val r = reviewMap[d]
                        appendLine("DATE: $d")
                        if (p != null) appendLine("  Quota: ${p.sentCount}/${p.quotaTarget} (${if (p.quotaHit) "HIT" else "MISSED"})")
                        if (r != null) {
                            appendLine("  Went Well: ${r.wentWell}")
                            appendLine("  Went Wrong: ${r.wentWrong}")
                        }
                        appendLine()
                    }
                }
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("PushBack Report", report))
                Toast.makeText(context, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
            }

            Spacer(Modifier.height(14.dp))

            // Filter Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "HITS", "MISSED", "WITH AAR").forEach { f ->
                    val isSelected = selectedFilter == f
                    item {
                        Box(
                            Modifier
                                .background(if (isSelected) colors.tape else colors.surface)
                                .border(1.dp, if (isSelected) colors.night else colors.hairline)
                                .clickable {
                                    feedback.click()
                                    selectedFilter = f
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                f,
                                style = typography.labelSmall.copy(
                                    color = if (isSelected) colors.tapeText else colors.muted,
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HairlineRule()
            Spacer(Modifier.height(8.dp))
        }

        if (filteredDates.isEmpty()) {
            item {
                Spacer(Modifier.height(30.dp))
                Text("No matching log entries found.", style = typography.mono, color = colors.muted)
            }
        }

        items(filteredDates, key = { it }) { date ->
            val plan = planMap[date]
            val review = reviewMap[date]
            DayLogCard(date = date, plan = plan, review = review, card = review?.let { cardFor(it) })
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun DayLogCard(
    date: String,
    plan: DayPlanEntity?,
    review: DayReviewEntity?,
    card: MentalityCard?,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography

    Column(Modifier.padding(vertical = 6.dp)) {
        PaperSheet(borderColor = if (plan?.quotaHit == true) colors.good else colors.hairline) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Eyebrow(date, colors.gold)
                if (plan != null) {
                    TrackChip(
                        label = if (plan.quotaHit) "QUOTA HIT • ${plan.sentCount}/${plan.quotaTarget}" else "MISSED • ${plan.sentCount}/${plan.quotaTarget}",
                        color = if (plan.quotaHit) colors.good else colors.debt,
                        filled = plan.quotaHit,
                    )
                }
            }

            if (plan != null && plan.ifThen.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("If-Then: ${plan.ifThen}", style = typography.mono.copy(fontSize = 11.sp), color = colors.muted)
            }

            if (review != null) {
                Spacer(Modifier.height(10.dp))
                Text("What went well:", style = typography.labelSmall, color = colors.good)
                Text(review.wentWell.ifBlank { "—" }, style = typography.bodySmall, color = colors.paper)
                Spacer(Modifier.height(4.dp))
                Text("What went wrong / waste:", style = typography.labelSmall, color = colors.debt)
                Text(review.wentWrong.ifBlank { "—" }, style = typography.bodySmall, color = colors.debt)

                if (card != null) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.hairline)
                            .padding(8.dp)
                    ) {
                        Text(
                            "“${card.line}” — ${card.thinker}",
                            style = typography.mono.copy(fontSize = 11.sp),
                            color = colors.gold,
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Text("No evening review logged.", style = typography.mono.copy(fontSize = 11.sp), color = colors.muted)
            }
        }
    }
}
