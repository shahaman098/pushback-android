package com.pushback.app.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pushback.app.AppViewModel
import com.pushback.app.data.Settings
import com.pushback.app.domain.FailureMode
import com.pushback.app.ui.components.RuledPaper
import com.pushback.app.ui.components.rememberTactileFeedback
import com.pushback.app.ui.screens.EveningScreen
import com.pushback.app.ui.screens.FocusScreen
import com.pushback.app.ui.screens.JobsScreen
import com.pushback.app.ui.screens.LogScreen
import com.pushback.app.ui.screens.MindScreen
import com.pushback.app.ui.screens.MoreScreen
import com.pushback.app.ui.screens.MorningScreen
import com.pushback.app.ui.screens.OnboardingScreen
import com.pushback.app.ui.screens.PatternsScreen
import com.pushback.app.ui.screens.SettingsScreen
import com.pushback.app.ui.screens.TasksScreen
import com.pushback.app.ui.screens.TodayScreen
import com.pushback.app.ui.theme.PbTheme
import com.pushback.app.ui.theme.PushBackTheme

object Routes {
    const val Onboarding = "onboarding"
    const val Today = "today"
    const val Jobs = "jobs"
    const val Focus = "focus"
    const val Log = "log"
    const val More = "more"
    const val Tasks = "tasks"
    const val Morning = "morning"
    const val Evening = "evening"
    const val Patterns = "patterns"
    const val Mind = "mind"
    const val Settings = "settings"
}

fun mapDeepLink(route: String?): String? = when (route) {
    "morning" -> Routes.Morning
    "evening" -> Routes.Evening
    "jobs" -> Routes.Jobs
    "focus" -> Routes.Focus
    "today" -> Routes.Today
    else -> null
}

@Composable
fun PushBackNav(vm: AppViewModel, settings: Settings, startRoute: String? = null) {
    val nav = rememberNavController()
    val today by vm.today.collectAsStateWithLifecycle()
    val jobs by vm.jobs.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val plan by vm.plan.collectAsStateWithLifecycle()
    val plans by vm.plans.collectAsStateWithLifecycle()
    val reviews by vm.reviews.collectAsStateWithLifecycle()
    val pending by vm.pendingInsight.collectAsStateWithLifecycle()
    val insights by vm.insights.collectAsStateWithLifecycle()
    val focus by vm.activeFocus.collectAsStateWithLifecycle()
    val liveSettings by vm.settings.collectAsStateWithLifecycle()
    val s = liveSettings ?: settings

    val feedback = rememberTactileFeedback(s.hapticsEnabled)

    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tabs = listOf(
        Routes.Today to "TODAY",
        Routes.Jobs to "JOBS",
        Routes.Focus to "FOCUS",
        Routes.Log to "LOG",
        Routes.More to "INDEX",
    )
    val showBar = tabs.any { it.first == route } && s.onboardingDone

    PbTheme(themeMode = s.themeMode) {
        val colors = PushBackTheme.colors
        val typography = PushBackTheme.typography

        RuledPaper {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                NavHost(
                    navController = nav,
                    startDestination = when {
                        !s.onboardingDone -> Routes.Onboarding
                        startRoute != null -> startRoute
                        else -> Routes.Today
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    composable(Routes.Onboarding) {
                        OnboardingScreen { outcome, quota, identity, ifThen, waste ->
                            vm.finishOnboarding(outcome, quota, identity, ifThen, waste)
                            nav.navigate(Routes.Morning) {
                                popUpTo(Routes.Onboarding) { inclusive = true }
                            }
                        }
                    }
                    composable(Routes.Today) {
                        TodayScreen(
                            snap = today,
                            requiredTasks = tasks,
                            onAddJob = vm::addJob,
                            onMarkSent = vm::markJobSent,
                            onSkip = vm::skipJob,
                            onToggleTask = vm::toggleTask,
                            onMorning = { nav.navigate(Routes.Morning) },
                            onEvening = { nav.navigate(Routes.Evening) },
                            onFocus = { nav.navigate(Routes.Focus) },
                            onJobs = { nav.navigate(Routes.Jobs) },
                            onPatterns = { nav.navigate(Routes.Patterns) },
                        )
                    }
                    composable(Routes.Jobs) {
                        JobsScreen(
                            jobs = jobs,
                            remaining = today?.requiredRemaining ?: 0,
                            onAdd = vm::addJob,
                            onStatus = vm::setJobStatus,
                            onDelete = vm::deleteJob,
                        )
                    }
                    composable(Routes.Focus) {
                        FocusScreen(
                            snapRemaining = today?.requiredRemaining ?: 0,
                            frog = tasks.firstOrNull { it.isFrog },
                            active = focus,
                            compoundMinutes = s.compoundMinutes,
                            watchdogOn = s.watchdogEnabled,
                            usageGranted = vm.hasUsageAccess(),
                            onStart = { taskId, minutes, cb -> vm.startFocus(taskId, minutes, cb) },
                            onEnd = vm::endFocus,
                        )
                    }
                    composable(Routes.Log) {
                        LogScreen(plans, reviews, vm::cardFor)
                    }
                    composable(Routes.More) {
                        MoreScreen(
                            onTasks = { nav.navigate(Routes.Tasks) },
                            onMorning = { nav.navigate(Routes.Morning) },
                            onEvening = { nav.navigate(Routes.Evening) },
                            onPatterns = { nav.navigate(Routes.Patterns) },
                            onMind = { nav.navigate(Routes.Mind) },
                            onSettings = { nav.navigate(Routes.Settings) },
                        )
                    }
                    composable(Routes.Tasks) {
                        TasksScreen(
                            tasks = tasks,
                            compoundLocked = today?.compoundLocked == true,
                            onAdd = vm::addTask,
                            onToggle = vm::toggleTask,
                            onPostpone = vm::postpone,
                            onDelete = vm::deleteTask,
                        )
                    }
                    composable(Routes.Morning) {
                        MorningScreen(s, plan, today?.patternLine) { w, o, ob, p, i, f, id ->
                            vm.completeMorning(w, o, ob, p, i, f, id)
                            nav.navigate(Routes.Today) {
                                popUpTo(Routes.Today) { inclusive = false }
                            }
                        }
                    }
                    composable(Routes.Evening) {
                        val mode = when {
                            (today?.sentToday ?: 0) == 0 -> FailureMode.NO_SEND
                            (today?.requiredRemaining ?: 0) > 0 -> FailureMode.FAKE_WORK
                            else -> FailureMode.NO_REVIEW
                        }
                        EveningScreen(
                            today,
                            vm.mentality.firstOrNull { it.failureMode == mode } ?: vm.mentality.firstOrNull(),
                        ) { supposed, happened, well, wrong, tmrw ->
                            vm.completeEvening(supposed, happened, well, wrong, tmrw)
                            nav.navigate(Routes.Today)
                        }
                    }
                    composable(Routes.Patterns) {
                        PatternsScreen(s, pending, insights, vm::acceptInsight, vm::rejectInsight)
                    }
                    composable(Routes.Mind) { MindScreen(vm.mentality) }
                    composable(Routes.Settings) {
                        SettingsScreen(
                            settings = s,
                            usageGranted = vm.hasUsageAccess(),
                            onUpdate = vm::updateSettings,
                            onSeedDemoData = vm::seedDemoData,
                        )
                    }
                }

                if (showBar) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(colors.night)
                            .border(width = 1.dp, color = colors.hairline.copy(alpha = 0.3f))
                            .navigationBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tabs.forEach { (tab, label) ->
                            val on = route == tab
                            val badgeCount = when (tab) {
                                Routes.Today -> today?.requiredRemaining?.takeIf { it > 0 }
                                Routes.Jobs -> jobs.count { it.status == com.pushback.app.domain.JobStatus.TO_APPLY }.takeIf { it > 0 }
                                else -> null
                            }

                            Box(
                                Modifier
                                    .clickable {
                                        feedback.click()
                                        nav.navigate(tab) {
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .then(
                                        if (on) Modifier
                                            .background(colors.tape)
                                            .border(1.dp, colors.tape)
                                        else Modifier,
                                    )
                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        label,
                                        style = typography.labelSmall.copy(fontSize = 9.5.sp, letterSpacing = 1.6.sp),
                                        color = if (on) colors.tapeText else colors.cream.copy(alpha = 0.6f),
                                    )
                                    if (badgeCount != null && !on) {
                                        Spacer(Modifier.width(4.dp))
                                        Box(
                                            Modifier
                                                .background(colors.debt)
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                "$badgeCount",
                                                style = typography.labelSmall.copy(fontSize = 8.sp, color = colors.cream),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
