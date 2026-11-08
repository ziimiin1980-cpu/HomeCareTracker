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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.data.entity.MaintenanceTask
import com.example.homecaretracker.ui.components.ApplianceErrorBanner
import com.example.homecaretracker.util.formatUtcDate
import com.example.homecaretracker.util.toUtcDate
import com.example.homecaretracker.viewmodel.HomeDashboardUiState
import com.example.homecaretracker.ui.theme.AppThemeMode
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun HomeScreen(
    state: HomeDashboardUiState,
    onAddAppliance: () -> Unit,
    onAddTask: () -> Unit,
    onOpenMaintenance: () -> Unit,
    onOpenHistory: () -> Unit,
    onRetry: () -> Unit,
    themeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit
) {
    val today = LocalDate.now(ZoneOffset.UTC)
    var showThemePicker by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Добро пожаловать",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Обзор",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = { showThemePicker = true }) {
                Icon(Icons.Filled.Settings, contentDescription = "Настройки темы")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "HomeCareTracker · забота о доме без лишних хлопот.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        when {
            state.isLoading -> CircularProgressIndicator()
            state.loadError != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ApplianceErrorBanner(state.loadError)
                OutlinedButton(onClick = onRetry) { Text("Повторить") }
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatisticCard("Устройств", state.applianceCount, Modifier.weight(1f))
                        StatisticCard("Активных задач", state.activeTaskCount, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatisticCard(
                            "Просрочено",
                            state.overdueTaskCount,
                            Modifier.weight(1f),
                            isWarning = state.overdueTaskCount > 0
                        )
                        StatisticCard("Выполнено", state.completedCount, Modifier.weight(1f))
                    }

                }
                Text(
                    "Ближайшее обслуживание",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (state.upcomingTasks.isEmpty()) {
                    Text("Активных задач пока нет.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.upcomingTasks, key = MaintenanceTask::id) { task ->
                            UpcomingTaskCard(
                                task = task,
                                applianceName = state.applianceNames[task.applianceId]
                                    ?: "Устройство удалено",
                                isOverdue = task.nextDueDate.toUtcDate().isBefore(today)
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onAddAppliance, modifier = Modifier.weight(1f)) {
                            Text("Добавить технику")
                        }
                        OutlinedButton(onClick = onAddTask, modifier = Modifier.weight(1f)) {
                            Text("Создать задачу")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onOpenMaintenance, modifier = Modifier.weight(1f)) {
                            Text("Все задачи")
                        }
                        OutlinedButton(onClick = onOpenHistory, modifier = Modifier.weight(1f)) {
                            Text("История")
                        }
                    }
                    if (showThemePicker) {
                        AlertDialog(
                            onDismissRequest = { showThemePicker = false },
                            title = { Text("Внешний вид") },
                            text = {
                                Column {
                                    AppThemeMode.entries.forEach { mode ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(mode.label, modifier = Modifier.weight(1f))
                                            RadioButton(
                                                selected = themeMode == mode,
                                                modifier = Modifier.testTag("theme_${mode.name}"),
                                                onClick = {
                                                    onThemeModeChanged(mode)
                                                    showThemePicker = false
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showThemePicker = false }) { Text("Готово") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: Int,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun UpcomingTaskCard(
    task: MaintenanceTask,
    applianceName: String,
    isOverdue: Boolean
) {
    Card(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Text(applianceName, color = MaterialTheme.colorScheme.primary)
            Text(
                text = if (isOverdue) {
                    "Просрочено · ${formatUtcDate(task.nextDueDate)}"
                } else {
                    "Запланировано · ${formatUtcDate(task.nextDueDate)}"
                },
                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
