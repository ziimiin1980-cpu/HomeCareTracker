package com.example.homecaretracker.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.data.entity.MaintenanceHistory
import com.example.homecaretracker.ui.components.ApplianceErrorBanner
import com.example.homecaretracker.util.formatUtcDate
import com.example.homecaretracker.util.toUtcDate
import com.example.homecaretracker.viewmodel.HistoryUiState
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onRetry: () -> Unit
) {
    var selectedApplianceId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedPeriodDays by rememberSaveable { mutableIntStateOf(0) }
    var applianceMenuExpanded by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<MaintenanceHistory?>(null) }
    val applianceOptions = remember(state.records) {
        state.records
            .distinctBy(MaintenanceHistory::applianceId)
            .map { it.applianceId to it.applianceName }
    }
    val cutoffDate = LocalDate.now(ZoneOffset.UTC).minusDays(selectedPeriodDays.toLong())
    val filteredRecords = state.records.filter { record ->
        (selectedApplianceId == null || record.applianceId == selectedApplianceId) &&
            (selectedPeriodDays == 0 || !record.completedAt.toUtcDate().isBefore(cutoffDate))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("История", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        when {
            state.isLoading -> CircularProgressIndicator()
            state.loadError != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ApplianceErrorBanner(state.loadError)
                OutlinedButton(onClick = onRetry) { Text("Повторить") }
            }
            state.records.isEmpty() -> Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("История пока пуста", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "После выполнения задачи запись появится здесь и останется доступной.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Устройство", style = MaterialTheme.typography.labelLarge)
                    Row {
                        OutlinedButton(onClick = { applianceMenuExpanded = true }) {
                            Text(
                                selectedApplianceId?.let { id ->
                                    applianceOptions.firstOrNull { it.first == id }?.second
                                } ?: "Все устройства",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DropdownMenu(
                            expanded = applianceMenuExpanded,
                            onDismissRequest = { applianceMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Все устройства") },
                                onClick = {
                                    selectedApplianceId = null
                                    applianceMenuExpanded = false
                                }
                            )
                            applianceOptions.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    onClick = {
                                        selectedApplianceId = id
                                        applianceMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Text("Период", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0 to "Все", 30 to "30 дней", 90 to "90 дней", 365 to "Год")
                            .forEach { (days, label) ->
                                FilterChip(
                                    selected = selectedPeriodDays == days,
                                    onClick = { selectedPeriodDays = days },
                                    label = { Text(label) }
                                )
                            }
                    }
                }
                if (filteredRecords.isEmpty()) {
                    Text("За выбранный период записей нет.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredRecords, key = MaintenanceHistory::id) { record ->
                            HistoryCard(
                                record = record,
                                onClick = { selectedRecord = record },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(180),
                                    placementSpec = tween(180),
                                    fadeOutSpec = tween(120)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    selectedRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRecord = null },
            title = { Text(record.taskTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Устройство: ${record.applianceName.ifBlank { "Не указано" }}")
                    Text("Дата выполнения: ${formatUtcDate(record.completedAt)}")
                    record.description?.takeIf(String::isNotBlank)?.let {
                        Text("Описание: $it")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRecord = null }) { Text("Закрыть") }
            }
        )
    }
}

@Composable
private fun HistoryCard(
    record: MaintenanceHistory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(record.taskTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(record.applianceName.ifBlank { "Устройство не указано" }, color = MaterialTheme.colorScheme.primary)
            Text(formatUtcDate(record.completedAt))
            record.description?.takeIf(String::isNotBlank)?.let { Text(it) }
        }
    }
}
