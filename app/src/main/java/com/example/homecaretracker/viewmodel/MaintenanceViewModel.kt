package com.example.homecaretracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.homecaretracker.data.entity.Appliance
import com.example.homecaretracker.data.entity.MaintenanceHistory
import com.example.homecaretracker.data.entity.MaintenanceTask
import com.example.homecaretracker.data.repository.ApplianceRepository
import com.example.homecaretracker.data.repository.MaintenanceTaskRepository
import com.example.homecaretracker.util.nextDueDateAfterCompletion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintenanceUiState(
    val tasks: List<MaintenanceTask> = emptyList(),
    val appliances: List<Appliance> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val loadError: String? = null,
    val operationError: String? = null,
    val operationMessage: String? = null
)

class MaintenanceViewModel(
    private val taskRepository: MaintenanceTaskRepository,
    private val applianceRepository: ApplianceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeData()
    }

    fun retryLoading() {
        if (observeJob?.isActive != true) observeData()
    }

    fun clearMessages() {
        _uiState.update { it.copy(operationError = null, operationMessage = null) }
    }

    fun saveTask(task: MaintenanceTask, onSaved: () -> Unit) {
        if (task.title.isBlank() || task.intervalDays <= 0) {
            _uiState.update {
                it.copy(operationError = "Проверьте название и периодичность задачи.")
            }
            return
        }
        if (_uiState.value.appliances.none { it.id == task.applianceId }) {
            _uiState.update {
                it.copy(operationError = "Выберите существующее устройство.")
            }
            return
        }
        performOperation(onSuccess = onSaved) {
            if (task.id == 0L) {
                taskRepository.add(task)
            } else if (taskRepository.update(task) == 0) {
                throw IllegalStateException("Задача больше не существует.")
            }
        }
    }

    fun deleteTask(taskId: Long, onDeleted: () -> Unit) {
        performOperation(onSuccess = onDeleted) {
            if (taskRepository.delete(taskId) == 0) {
                throw IllegalStateException("Задача больше не существует.")
            }
        }
    }

    fun setTaskActive(task: MaintenanceTask, isActive: Boolean) {
        performOperation {
            if (taskRepository.update(task.copy(isActive = isActive)) == 0) {
                throw IllegalStateException("Задача больше не существует.")
            }
        }
    }

    fun completeTask(taskId: Long) {
        performOperation {
            val task = taskRepository.getById(taskId)
                ?: throw IllegalStateException("Задача больше не существует.")
            val appliance = applianceRepository.getById(task.applianceId)
                ?: throw IllegalStateException("Устройство больше не существует.")
            val completedAt = System.currentTimeMillis()
            val nextDueDate = nextDueDateAfterCompletion(
                scheduledDate = task.nextDueDate,
                completedAt = completedAt,
                intervalDays = task.intervalDays
            )
            val history = MaintenanceHistory(
                applianceId = appliance.id,
                applianceName = appliance.name,
                taskTitle = task.title,
                description = task.description,
                completedAt = completedAt
            )
            if (!taskRepository.complete(task.copy(nextDueDate = nextDueDate), history)) {
                throw IllegalStateException("Задача больше не существует.")
            }
            _uiState.update {
                it.copy(operationMessage = "Обслуживание записано в историю.")
            }
        }
    }

    private fun observeData() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            combine(
                taskRepository.observeAll(),
                applianceRepository.observeAll()
            ) { tasks, appliances -> tasks to appliances }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = exception.message
                                ?: "Не удалось загрузить задачи обслуживания."
                        )
                    }
                }
                .collect { (tasks, appliances) ->
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            appliances = appliances,
                            isLoading = false,
                            loadError = null
                        )
                    }
                }
        }
    }

    private fun performOperation(
        onSuccess: (() -> Unit)? = null,
        operation: suspend () -> Unit
    ) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, operationError = null, operationMessage = null)
            }
            try {
                operation()
                onSuccess?.invoke()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        operationError = exception.message
                            ?: "Не удалось выполнить операцию. Попробуйте ещё раз."
                    )
                }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    class Factory(
        private val taskRepository: MaintenanceTaskRepository,
        private val applianceRepository: ApplianceRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MaintenanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MaintenanceViewModel(taskRepository, applianceRepository) as T
            }
            throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
        }
    }
}
