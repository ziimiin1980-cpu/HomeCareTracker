package com.example.homecaretracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.homecaretracker.data.entity.MaintenanceHistory
import com.example.homecaretracker.data.repository.MaintenanceHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val records: List<MaintenanceHistory> = emptyList(),
    val isLoading: Boolean = true,
    val loadError: String? = null
)

class HistoryViewModel(
    private val repository: MaintenanceHistoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeHistory()
    }

    fun retryLoading() {
        if (observeJob?.isActive != true) observeHistory()
    }

    private fun observeHistory() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            repository.observeAll()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = exception.message ?: "Не удалось загрузить историю."
                        )
                    }
                }
                .collect { records ->
                    _uiState.update {
                        it.copy(records = records, isLoading = false, loadError = null)
                    }
                }
        }
    }

    class Factory(
        private val repository: MaintenanceHistoryRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unsupported ViewModel class: ${modelClass.name}")
        }
    }
}
