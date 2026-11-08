package com.example.homecaretracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.data.entity.MaintenanceTask
import com.example.homecaretracker.ui.components.ApplianceErrorBanner
import com.example.homecaretracker.util.formatUtcDate
import com.example.homecaretracker.util.toUtcDate
import com.example.homecaretracker.viewmodel.MaintenanceUiState
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun MaintenanceScreen(
    state: MaintenanceUiState,
    onAddClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onToggle: (MaintenanceTask, Boolean) -> Unit,
    onComplete: (Long) -> Unit,
    onRetry: () -> Unit,
    onErrorDismissed: () -> Unit
) {
    val today = LocalDate.now(ZoneOffset.UTC)
    val inactive = state.tasks.filterNot(MaintenanceTask::isActive)
    val overdue = state.tasks.filter {
        it.isActive && it.nextDueDate.toUtcDate().isBefore(today)
    }.sortedBy(MaintenanceTask::nextDueDate)
    val upcoming = state.tasks.filter {
        it.isActive && !it.nextDueDate.toUtcDate().isBefore(today)
    }.sortedBy(MaintenanceTask::nextDueDate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Обслуживание",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(onClick = onAddClick) { Text("Добавить") }
        }
        state.operationError?.let {
            ApplianceErrorBanner(it, onDismiss = onErrorDismissed)
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
        when {
            state.isLoading -> CircularProgressIndicator()
            state.loadError != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ApplianceErrorBanner(state.loadError)
                OutlinedButton(onClick = onRetry) { Text("Повторить") }
            }
            state.tasks.isEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Задач пока нет", style = MaterialTheme.typography.titleLarge)
                Text("Создайте задачу, чтобы не забывать о регулярном уходе за техникой.")
                if (state.appliances.isEmpty()) {
                    Text("Сначала добавьте устройство во вкладке «Моя техника».")
                } else {
                    OutlinedButton(onClick = onAddClick) { Text("Создать задачу") }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (overdue.isNotEmpty()) {
                    item { SectionTitle("Просроченные") }
                    items(overdue, key = MaintenanceTask::id) { task ->
                        MaintenanceTaskCard(
                            task = task,
                            applianceName = state.appliances
                                .firstOrNull { it.id == task.applianceId }?.name
                                ?: "Устройство удалено",
                            isOverdue = true,
                            onClick = { onTaskClick(task.id) },
                            onToggle = { onToggle(task, it) },
                            onComplete = { onComplete(task.id) }
                        )
                    }
                }
                if (upcoming.isNotEmpty()) {
                    item { SectionTitle("Предстоящие") }
                    items(upcoming, key = MaintenanceTask::id) { task ->
                        MaintenanceTaskCard(
                            task = task,
                            applianceName = state.appliances
                                .firstOrNull { it.id == task.applianceId }?.name
                                ?: "Устройство удалено",
                            isOverdue = false,
                            onClick = { onTaskClick(task.id) },
                            onToggle = { onToggle(task, it) },
                            onComplete = { onComplete(task.id) }
                        )
                    }
                }
                if (inactive.isNotEmpty()) {
                    item { SectionTitle("Неактивные") }
                    items(inactive.sortedBy(MaintenanceTask::nextDueDate), key = MaintenanceTask::id) { task ->
                        MaintenanceTaskCard(
                            task = task,
                            applianceName = state.appliances
                                .firstOrNull { it.id == task.applianceId }?.name
                                ?: "Устройство удалено",
                            isOverdue = false,
                            onClick = { onTaskClick(task.id) },
                            onToggle = { onToggle(task, it) },
                            onComplete = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun MaintenanceTaskCard(
    task: MaintenanceTask,
    applianceName: String,
    isOverdue: Boolean,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onComplete: (() -> Unit)?
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isOverdue) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    Text(
                        text = if (isOverdue) "Просрочено" else "Активна",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }
            Text(applianceName, color = MaterialTheme.colorScheme.primary)
            task.description?.takeIf(String::isNotBlank)?.let { Text(it) }
            Text(
                text = "Следующее обслуживание: ${formatUtcDate(task.nextDueDate)}" +
                    if (isOverdue) " · Просрочено" else "",
                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text("Каждые ${task.intervalDays} дн.")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Активна", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = task.isActive,
                        onCheckedChange = onToggle
                    )
                }
                if (onComplete != null) {
                    OutlinedButton(onClick = onComplete) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Выполнено")
                    }
                }
            }
        }
    }
}
