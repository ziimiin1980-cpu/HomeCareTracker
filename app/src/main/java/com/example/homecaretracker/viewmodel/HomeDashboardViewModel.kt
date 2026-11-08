package com.example.homecaretracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.homecaretracker.data.entity.MaintenanceTask
import com.example.homecaretracker.data.repository.ApplianceRepository
import com.example.homecaretracker.data.repository.MaintenanceHistoryRepository
import com.example.homecaretracker.data.repository.MaintenanceTaskRepository
import com.example.homecaretracker.util.toUtcDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

data class HomeDashboardUiState(
    val applianceCount: Int = 0,
    val activeTaskCount: Int = 0,
    val overdueTaskCount: Int = 0,
    val completedCount: Int = 0,
    val upcomingTasks: List<MaintenanceTask> = emptyList(),
    val applianceNames: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = true,
    val loadError: String? = null
)

class HomeDashboardViewModel(
    private val applianceRepository: ApplianceRepository,
    private val taskRepository: MaintenanceTaskRepository,
    private val historyRepository: MaintenanceHistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeDashboardUiState())
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeData()
    }

    fun retryLoading() {
        if (observeJob?.isActive != true) {
            observeData()
        }
    }

    private fun observeData() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            combine(
                applianceRepository.observeAll(),
                taskRepository.observeAll(),
                historyRepository.observeAll()
            ) { appliances, tasks, history ->
                val today = LocalDate.now(ZoneOffset.UTC)
                val activeTasks = tasks.filter(MaintenanceTask::isActive)
                val upcoming = activeTasks.sortedWith(
                    compareBy<MaintenanceTask> {
                        if (it.nextDueDate.toUtcDate().isBefore(today)) 0 else 1
                    }.thenBy { it.nextDueDate }
                )
                HomeDashboardUiState(
                    applianceCount = appliances.size,
                    activeTaskCount = activeTasks.size,
                    overdueTaskCount = activeTasks.count {
                        it.nextDueDate.toUtcDate().isBefore(today)
                    },
                    completedCount = history.size,
                    upcomingTasks = upcoming.take(5),
                    applianceNames = appliances.associate { it.id to it.name },
                    isLoading = false
                )
            }.catch { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = exception.message ?: "Не удалось загрузить главную страницу."
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    class Factory(
        private val applianceRepository: ApplianceRepository,
        private val taskRepository: MaintenanceTaskRepository,
        private val historyRepository: MaintenanceHistoryRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeDashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeDashboardViewModel(
                    applianceRepository,
                    taskRepository,
                    historyRepository
                ) as T
            }
            throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
        }
    }
}
