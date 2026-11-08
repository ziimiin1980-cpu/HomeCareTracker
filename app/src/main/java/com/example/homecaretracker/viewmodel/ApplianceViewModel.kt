package com.example.homecaretracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.homecaretracker.data.entity.Appliance
import com.example.homecaretracker.data.repository.ApplianceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApplianceUiState(
    val appliances: List<Appliance> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val loadError: String? = null,
    val operationError: String? = null
)

class ApplianceViewModel(
    private val repository: ApplianceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApplianceUiState())
    val uiState: StateFlow<ApplianceUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeAppliances()
    }

    fun retryLoading() {
        if (observeJob?.isActive != true) {
            observeAppliances()
        }
    }

    fun clearOperationError() {
        _uiState.update { it.copy(operationError = null) }
    }

    fun saveAppliance(
        appliance: Appliance,
        onSaved: () -> Unit
    ) {
        performOperation(onSuccess = onSaved) {
            if (appliance.id == 0L) {
                repository.add(appliance)
            } else if (repository.update(appliance) == 0) {
                throw IllegalStateException("Устройство больше не существует.")
            }
        }
    }

    fun deleteAppliance(
        applianceId: Long,
        onDeleted: () -> Unit
    ) {
        performOperation(onSuccess = onDeleted) {
            if (repository.delete(applianceId) == 0) {
                throw IllegalStateException("Устройство больше не существует.")
            }
        }
    }

    private fun observeAppliances() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            repository.observeAll()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = exception.message
                                ?: "Не удалось загрузить список техники."
                        )
                    }
                }
                .collect { appliances ->
                    _uiState.update {
                        it.copy(
                            appliances = appliances,
                            isLoading = false,
                            loadError = null
                        )
                    }
                }
        }
    }

    private fun performOperation(
        onSuccess: () -> Unit,
        operation: suspend () -> Unit
    ) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, operationError = null) }
            try {
                operation()
                onSuccess()
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
        private val repository: ApplianceRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ApplianceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ApplianceViewModel(repository) as T
            }
            throw IllegalArgumentException(
                "Unsupported ViewModel class: ${modelClass.name}"
            )
        }
    }
}
