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
import com.pushback.app.data.JobEntity
import com.pushback.app.domain.Dates
import com.pushback.app.domain.JobParser
import com.pushback.app.domain.JobStatus
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun JobsScreen(
    jobs: List<JobEntity>,
    remaining: Int,
    onAdd: (String, String, String) -> Unit,
    onStatus: (JobEntity, JobStatus) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<JobStatus?>(null) }
    var showAddForm by remember { mutableStateOf(false) }

    val filteredJobs = jobs.filter { job ->
        val matchesFilter = selectedFilter == null || job.status == selectedFilter
        val matchesSearch = searchQuery.isBlank() ||
                job.company.contains(searchQuery, ignoreCase = true) ||
                job.role.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        item {
            Masthead(
                kicker = "APPLICATION LEDGER",
                date = if (remaining > 0) "$remaining STILL REQUIRED TODAY" else "DAILY QUOTA CLEARED",
            )
            Spacer(Modifier.height(8.dp))
            Text("Pipeline Ledger", style = typography.title)
            Text(
                if (remaining > 0)
                    "$remaining sends still required today. A listing row is not a send. Mark status to update the press."
                else
                    "Quota hit. Keep the pipeline honest and follow up on warm leads.",
                style = typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.height(14.dp))

            // Quick Add Toggle / Form
            PaperSheet(borderColor = colors.gold) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showAddForm = !showAddForm },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Eyebrow(if (showAddForm) "NEW LISTING ENTRY" else "+ ADD NEW TARGET LISTING", colors.gold)
                    Text(
                        if (showAddForm) "COLLAPSE" else "EXPAND",
                        style = typography.labelSmall,
                        color = colors.muted,
                    )
                }

                if (showAddForm) {
                    Spacer(Modifier.height(12.dp))
                    PbField(
                        value = url,
                        onValueChange = { input ->
                            url = input
                            val parsed = JobParser.parse(input)
                            if (parsed.company.isNotBlank() && company.isBlank()) company = parsed.company
                            if (parsed.role.isNotBlank() && role.isBlank()) role = parsed.role
                        },
                        hint = "Job Listing URL (Auto-extracts company & role)",
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PbField(
                            value = company,
                            onValueChange = { company = it },
                            hint = "Company",
                            modifier = Modifier.weight(1f),
                        )
                        PbField(
                            value = role,
                            onValueChange = { role = it },
                            hint = "Role",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PbButton("Add to Pipeline", Modifier.fillMaxWidth()) {
                        if (company.isNotBlank() && role.isNotBlank()) {
                            feedback.stampSlam()
                            onAdd(company.trim(), role.trim(), url.trim())
                            company = ""; role = ""; url = ""
                            showAddForm = false
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Search Bar
            PbField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                hint = "Search companies, roles...",
            )

            Spacer(Modifier.height(12.dp))

            // Status Filter Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    val allSelected = selectedFilter == null
                    Box(
                        Modifier
                            .background(if (allSelected) colors.tape else colors.surface)
                            .border(1.dp, if (allSelected) colors.night else colors.hairline)
                            .clickable { selectedFilter = null }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "ALL (${jobs.size})",
                            style = typography.labelSmall.copy(
                                color = if (allSelected) colors.tapeText else colors.muted,
                            ),
                        )
                    }
                }
                items(JobStatus.entries) { st ->
                    val isSelected = selectedFilter == st
                    val count = jobs.count { it.status == st }
                    Box(
                        Modifier
                            .background(if (isSelected) st.color(colors) else colors.surface)
                            .border(1.dp, if (isSelected) st.color(colors) else colors.hairline)
                            .clickable { selectedFilter = if (isSelected) null else st }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "${st.label()} ($count)",
                            style = typography.labelSmall.copy(
                                color = if (isSelected) colors.cream else st.color(colors),
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HairlineRule()
            Spacer(Modifier.height(8.dp))
        }

        if (filteredJobs.isEmpty()) {
            item {
                Spacer(Modifier.height(30.dp))
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        if (jobs.isEmpty()) "Empty pipeline. That is not a strategy." else "No listings matching criteria.",
                        style = typography.body,
                        color = colors.debt,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add a listing above or paste a job board link.",
                        style = typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }
        }

        items(filteredJobs, key = { it.id }) { job ->
            JobRow(job, onStatus, onDelete)
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun JobRow(
    job: JobEntity,
    onStatus: (JobEntity, JobStatus) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val context = LocalContext.current
    val feedback = rememberTactileFeedback(true)

    val dateFormatted = remember(job.createdAt) {
        val dt = Instant.ofEpochMilli(job.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        dt.format(DateTimeFormatter.ofPattern("MMM d"))
    }

    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        PaperSheet(borderColor = if (job.status == JobStatus.APPLIED || job.status == JobStatus.OFFER) colors.good else colors.hairline) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    job.company.uppercase(),
                    style = typography.label.copy(fontSize = 11.sp, color = colors.gold),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateFormatted, style = typography.mono.copy(fontSize = 10.sp), color = colors.muted)
                    Spacer(Modifier.width(8.dp))
                    TrackChip(job.status.label(), job.status.color(colors), filled = true)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(job.role, style = typography.title.copy(fontSize = 22.sp))
            if (job.url.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(job.url, style = typography.mono.copy(fontSize = 10.5.sp), color = colors.muted, maxLines = 1)
            }
            if (job.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Notes: ${job.notes}",
                    style = typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = colors.muted,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Pipeline Status Stepper
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                JobStatus.entries.forEach { st ->
                    val on = job.status == st
                    Box(
                        Modifier
                            .weight(1f)
                            .background(if (on) st.color(colors) else colors.surfaceElevated)
                            .border(1.dp, if (on) st.color(colors) else colors.hairline)
                            .clickable {
                                feedback.click()
                                onStatus(job, st)
                            }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            st.label(),
                            style = typography.labelSmall.copy(
                                fontSize = 8.sp,
                                color = if (on) colors.cream else st.color(colors),
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (job.url.isNotBlank()) {
                    PbGhostButton("Open Listing", Modifier.weight(1f)) {
                        val raw = if (job.url.startsWith("http")) job.url else "https://${job.url}"
                        val parsed = runCatching { Uri.parse(raw) }.getOrNull()
                        if (parsed != null) {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, parsed).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    }
                }
                if (job.status == JobStatus.TO_APPLY) {
                    PbButton("I Sent This ✓", Modifier.weight(1f), color = colors.gold) {
                        feedback.stampSlam()
                        onStatus(job, JobStatus.APPLIED)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "REMOVE FROM PIPELINE",
                    style = typography.labelSmall.copy(color = colors.muted),
                    modifier = Modifier.clickable {
                        feedback.warning()
                        onDelete(job.id)
                    },
                )
            }
        }
    }
}

private fun JobStatus.label() = when (this) {
    JobStatus.TO_APPLY -> "TO APPLY"
    JobStatus.APPLIED -> "SENT"
    JobStatus.FOLLOWED_UP -> "FOLLOW-UP"
    JobStatus.INTERVIEW -> "INTERVIEW"
    JobStatus.OFFER -> "OFFER"
    JobStatus.REJECTED -> "ARCHIVED"
}

@Composable
private fun JobStatus.color(colors: com.pushback.app.ui.theme.PbColors) = when (this) {
    JobStatus.TO_APPLY -> colors.muted
    JobStatus.APPLIED -> colors.gold
    JobStatus.FOLLOWED_UP -> colors.paper
    JobStatus.INTERVIEW -> colors.good
    JobStatus.OFFER -> colors.good
    JobStatus.REJECTED -> colors.debt
}
