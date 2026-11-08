package com.example.homecaretracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.data.entity.MaintenanceTask
import com.example.homecaretracker.ui.components.ApplianceErrorBanner
import com.example.homecaretracker.util.formatUtcDate
import com.example.homecaretracker.viewmodel.MaintenanceUiState

@Composable
fun MaintenanceTaskDetailScreen(
    task: MaintenanceTask?,
    applianceName: String?,
    state: MaintenanceUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onErrorDismissed: () -> Unit
) {
    var showDeleteConfirmation by remember(task?.id) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Обслуживание") }
        if (task == null) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.loadError != null -> ApplianceErrorBanner(state.loadError)
                else -> Text("Задача не найдена.")
            }
            return@Column
        }
        Text(task.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(applianceName ?: "Устройство удалено", color = MaterialTheme.colorScheme.primary)
        Text(task.description?.takeIf(String::isNotBlank) ?: "Описание не указано.")
        Text("Следующее обслуживание: ${formatUtcDate(task.nextDueDate)}")
        Text("Периодичность: каждые ${task.intervalDays} дней")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(if (task.isActive) "Активна" else "Неактивна")
            Switch(checked = task.isActive, onCheckedChange = onToggle)
        }
        state.operationMessage?.let { message ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(message, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onErrorDismissed) { Text("Закрыть") }
            }
        }
        state.operationError?.let { ApplianceErrorBanner(it, onDismiss = onErrorDismissed) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) { Text("Редактировать") }
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                enabled = task.isActive && !state.isSaving
            ) { Text("Выполнено") }
        }
        OutlinedButton(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) { Text("Удалить задачу") }
    }
    if (showDeleteConfirmation && task != null) {
        AlertDialog(
            onDismissRequest = { if (!state.isSaving) showDeleteConfirmation = false },
            title = { Text("Удалить задачу?") },
            text = { Text("«${task.title}» будет удалена. Записи в истории останутся.") },
            confirmButton = {
                TextButton(onClick = onDelete, enabled = !state.isSaving) {
                    Text("Подтвердить удаление")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    enabled = !state.isSaving
                ) { Text("Отмена") }
            }
        )
    }
}
