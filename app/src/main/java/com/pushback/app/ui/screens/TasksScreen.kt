package com.pushback.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.data.TaskEntity
import com.pushback.app.domain.TaskTrack
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
fun TasksScreen(
    tasks: List<TaskEntity>,
    compoundLocked: Boolean,
    onAdd: (String, TaskTrack, Boolean) -> Unit,
    onToggle: (TaskEntity) -> Unit,
    onPostpone: (TaskEntity, String) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    var title by remember { mutableStateOf("") }
    var track by remember { mutableStateOf(TaskTrack.REQUIRED) }
    var isFrog by remember { mutableStateOf(false) }
    var postponeFor by remember { mutableStateOf<TaskEntity?>(null) }
    var reason by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<TaskTrack?>(null) }

    val debtTasks = tasks.filter { it.title.startsWith("DEBT:") }
    val regularTasks = tasks.filter { !it.title.startsWith("DEBT:") }
    val filteredTasks = regularTasks.filter { selectedFilter == null || it.track == selectedFilter }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        item {
            Masthead("TASK PROTOCOL", "REQUIRED • COMPOUND • OPTIONAL")
            Spacer(Modifier.height(8.dp))
            Text("Task Ledger", style = typography.title)
            Text(
                "Required cannot be skipped. Compound is locked until daily quota is hit. Optional is how people hide.",
                style = typography.bodySmall,
                color = colors.muted,
            )
            Spacer(Modifier.height(14.dp))

            // Add Task Form
            PaperSheet(borderColor = colors.gold) {
                Eyebrow("CREATE PROTOCOL TASK", colors.gold)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskTrack.entries.forEach { t ->
                        val on = track == t
                        Box(
                            Modifier
                                .background(if (on) t.trackColor(colors) else colors.surface)
                                .border(1.dp, if (on) t.trackColor(colors) else colors.hairline)
                                .clickable {
                                    feedback.click()
                                    track = t
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                t.name,
                                style = typography.labelSmall.copy(color = if (on) colors.cream else colors.paper),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                PbField(title, { title = it }, "What must happen today")
                Spacer(Modifier.height(10.dp))
                PbButton("Add ${track.name} Task", Modifier.fillMaxWidth()) {
                    if (title.isNotBlank()) {
                        feedback.stampSlam()
                        onAdd(title.trim(), track, isFrog || (track == TaskTrack.REQUIRED && tasks.none { it.isFrog }))
                        title = ""
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Filter Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    val allSelected = selectedFilter == null
                    Box(
                        Modifier
                            .background(if (allSelected) colors.tape else colors.surface)
                            .border(1.dp, if (allSelected) colors.night else colors.hairline)
                            .clickable {
                                feedback.click()
                                selectedFilter = null
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "ALL (${tasks.size})",
                            style = typography.labelSmall.copy(color = if (allSelected) colors.tapeText else colors.muted),
                        )
                    }
                }
                items(TaskTrack.entries) { t ->
                    val isSelected = selectedFilter == t
                    val count = regularTasks.count { it.track == t }
                    Box(
                        Modifier
                            .background(if (isSelected) t.trackColor(colors) else colors.surface)
                            .border(1.dp, if (isSelected) t.trackColor(colors) else colors.hairline)
                            .clickable {
                                feedback.click()
                                selectedFilter = if (isSelected) null else t
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "${t.name} ($count)",
                            style = typography.labelSmall.copy(color = if (isSelected) colors.cream else t.trackColor(colors)),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HairlineRule()
            Spacer(Modifier.height(8.dp))
        }

        // Overdue Debt Section
        if (debtTasks.isNotEmpty()) {
            item {
                Eyebrow("UNRESOLVED DEBT (ROLLED FORWARD)", colors.debt)
                Spacer(Modifier.height(6.dp))
            }
            items(debtTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    locked = false,
                    onToggle = onToggle,
                    onPostponePrompt = { postponeFor = task },
                    onDelete = onDelete,
                )
            }
            item {
                Spacer(Modifier.height(14.dp))
                HairlineRule()
                Spacer(Modifier.height(8.dp))
            }
        }

        // Regular Tasks
        items(filteredTasks, key = { it.id }) { task ->
            val locked = task.track == TaskTrack.COMPOUND && compoundLocked && task.doneAt == null
            TaskItem(
                task = task,
                locked = locked,
                onToggle = onToggle,
                onPostponePrompt = { postponeFor = task },
                onDelete = onDelete,
            )
        }

        // Postpone Dialog Sheet
        postponeFor?.let { task ->
            item {
                Spacer(Modifier.height(12.dp))
                PaperSheet(borderColor = colors.debt) {
                    Eyebrow("POSTPONE ACCOUNTABILITY", colors.debt)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Postponing: ${task.title}",
                        style = typography.title.copy(fontSize = 18.sp),
                        color = colors.paper,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Write why you are postponing. This reason will be stamped in your evening after-action review.",
                        style = typography.bodySmall,
                        color = colors.muted,
                    )
                    Spacer(Modifier.height(10.dp))
                    PbField(reason, { reason = it }, "Enter exact reason for postponement")
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PbButton("Confirm Postpone", Modifier.weight(1f), color = colors.debt) {
                            if (reason.isNotBlank()) {
                                feedback.warning()
                                onPostpone(task, reason.trim())
                                postponeFor = null
                                reason = ""
                            }
                        }
                        PbGhostButton("Cancel", Modifier.weight(0.7f)) {
                            postponeFor = null
                            reason = ""
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun TaskItem(
    task: TaskEntity,
    locked: Boolean,
    onToggle: (TaskEntity) -> Unit,
    onPostponePrompt: () -> Unit,
    onDelete: (Long) -> Unit,
) {
    val colors = PushBackTheme.colors
    val typography = PushBackTheme.typography
    val feedback = rememberTactileFeedback(true)

    val isDone = task.doneAt != null

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        PaperSheet(borderColor = if (isDone) colors.good else if (task.isFrog) colors.debt else colors.hairline) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !locked) {
                        feedback.click()
                        onToggle(task)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TrackChip(
                        label = if (task.isFrog) "★ THE FROG" else task.track.name,
                        color = if (task.isFrog) colors.debt else task.track.trackColor(colors),
                        filled = task.isFrog || isDone,
                    )
                }
                TrackChip(
                    label = if (isDone) "COMPLETED" else if (locked) "LOCKED (QUOTA DUE)" else "OPEN",
                    color = if (isDone) colors.good else if (locked) colors.muted else colors.gold,
                    filled = isDone,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                task.title,
                style = typography.body.copy(
                    fontSize = 17.sp,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = if (isDone) colors.muted else colors.paper,
            )
            if (!task.postponedReason.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Postponed note: ${task.postponedReason}", style = typography.mono.copy(fontSize = 11.sp), color = colors.debt)
            }

            if (!isDone) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "POSTPONE WITH REASON →",
                        style = typography.labelSmall.copy(color = colors.muted),
                        modifier = Modifier.clickable {
                            feedback.click()
                            onPostponePrompt()
                        },
                    )
                    Text(
                        "DELETE",
                        style = typography.labelSmall.copy(color = colors.muted),
                        modifier = Modifier.clickable {
                            feedback.warning()
                            onDelete(task.id)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskTrack.trackColor(colors: com.pushback.app.ui.theme.PbColors) = when (this) {
    TaskTrack.REQUIRED -> colors.debt
    TaskTrack.COMPOUND -> colors.gold
    TaskTrack.OPTIONAL -> colors.muted
}
