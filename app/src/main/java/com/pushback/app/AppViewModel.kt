package com.pushback.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pushback.app.data.DayPlanEntity
import com.pushback.app.data.DayReviewEntity
import com.pushback.app.data.FocusSessionEntity
import com.pushback.app.data.JobEntity
import com.pushback.app.data.PatternInsightEntity
import com.pushback.app.data.Repository
import com.pushback.app.data.Settings
import com.pushback.app.data.TaskEntity
import com.pushback.app.domain.JobStatus
import com.pushback.app.domain.TaskTrack
import com.pushback.app.domain.TodaySnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AppViewModel(private val repo: Repository) : ViewModel() {
    val today: StateFlow<TodaySnapshot?> = repo.todaySnapshot().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null,
    )
    val jobs: StateFlow<List<JobEntity>> = repo.jobs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val tasks: StateFlow<List<TaskEntity>> = repo.tasksFor().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val plan: StateFlow<DayPlanEntity?> = repo.planFor().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null,
    )
    val review: StateFlow<DayReviewEntity?> = repo.reviewFor().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null,
    )
    val plans: StateFlow<List<DayPlanEntity>> = repo.allPlans.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val reviews: StateFlow<List<DayReviewEntity>> = repo.allReviews.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val settings: StateFlow<Settings?> = repo.settings.flow.stateIn(
        viewModelScope, SharingStarted.Eagerly, null,
    )
    val pendingInsight: StateFlow<PatternInsightEntity?> = repo.pendingInsight.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null,
    )
    val insights: StateFlow<List<PatternInsightEntity>> = repo.insights.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )
    val activeFocus: StateFlow<FocusSessionEntity?> = repo.activeFocus.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null,
    )

    val mentality = repo.mentality.cards

    init {
        viewModelScope.launch { repo.ensureTodayPlan() }
    }

    fun completeMorning(
        wish: String, outcome: String, obstacle: String, plan: String, ifThen: String,
        frog: String, identity: String,
    ) = launch { repo.completeMorning(wish, outcome, obstacle, plan, ifThen, frog, identity) }

    fun completeEvening(supposed: String, happened: String, well: String, wrong: String, tomorrow: String) =
        launch { repo.completeEvening(supposed, happened, well, wrong, tomorrow) }

    fun addJob(company: String, role: String, url: String) = launch { repo.addJob(company, role, url) }
    fun setJobStatus(job: JobEntity, status: JobStatus) = launch { repo.updateJobStatus(job, status) }
    fun deleteJob(id: Long) = launch { repo.deleteJob(id) }
    fun markJobSent(id: Long) = launch { repo.markJobSent(id) }
    fun skipJob(id: Long) = launch { repo.skipJob(id) }

    fun addTask(title: String, track: TaskTrack, frog: Boolean = false) =
        launch { repo.addTask(title, track, frog) }
    fun toggleTask(task: TaskEntity) = launch { repo.toggleTask(task) }
    fun postpone(task: TaskEntity, reason: String) = launch { repo.postponeTask(task, reason) }
    fun deleteTask(id: Long) = launch { repo.deleteTask(id) }

    fun startFocus(taskId: Long?, minutes: Int, onId: (Long) -> Unit) = launch {
        val id = repo.startFocus(taskId, minutes)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onId(id) }
    }
    fun endFocus(id: Long) = launch { repo.endFocus(id) }

    fun acceptInsight(i: PatternInsightEntity) = launch { repo.acceptInsight(i) }
    fun rejectInsight(i: PatternInsightEntity) = launch { repo.rejectInsight(i) }

    fun finishOnboarding(outcome: String, quota: Int, identity: String, ifThen: String, waste: Set<String>) =
        launch { repo.finishOnboarding(outcome, quota, identity, ifThen, waste) }

    fun updateSettings(transform: (Settings) -> Settings) = launch { repo.updateSettings(transform) }
    fun seedDemoData() = launch { repo.seedDemoData() }

    fun hasUsageAccess() = repo.hasUsageAccess()
    fun remaining() = today.value?.requiredRemaining ?: 0
    fun cardFor(review: DayReviewEntity) = repo.cardForReview(review)

    fun hourNow(): Int = LocalDateTime.now().hour

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    class Factory(private val repo: Repository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(repo) as T
    }
}
