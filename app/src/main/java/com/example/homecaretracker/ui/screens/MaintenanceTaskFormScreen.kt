package com.example.homecaretracker.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.homecaretracker.data.entity.Appliance
import com.example.homecaretracker.data.entity.MaintenanceTask
import com.example.homecaretracker.ui.components.ApplianceErrorBanner
import com.example.homecaretracker.util.formatUtcDate
import com.example.homecaretracker.util.toUtcMillis
import com.example.homecaretracker.viewmodel.MaintenanceUiState
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceTaskFormScreen(
    task: MaintenanceTask?,
    appliances: List<Appliance>,
    state: MaintenanceUiState,
    onSave: (MaintenanceTask) -> Unit,
    onCancel: () -> Unit,
    onErrorDismissed: () -> Unit
) {
    var applianceId by remember(task?.id) { mutableStateOf(task?.applianceId) }
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var description by remember(task?.id) { mutableStateOf(task?.description.orEmpty()) }
    var interval by remember(task?.id) {
        mutableStateOf(task?.intervalDays?.toString().orEmpty())
    }
    var nextDueDate by remember(task?.id) {
        mutableStateOf(
            task?.nextDueDate ?: LocalDate.now(ZoneOffset.UTC).toUtcMillis()
        )
    }
    var isActive by remember(task?.id) { mutableStateOf(task?.isActive ?: true) }
    var notificationsEnabled by remember(task?.id) {
        mutableStateOf(task?.notificationsEnabled ?: true)
    }
    var submitted by remember(task?.id) { mutableStateOf(false) }
    var applianceMenuExpanded by remember { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }
    var permissionExplanationOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var notificationPermissionGranted by remember {
        mutableStateOf(hasNotificationPermission(context))
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextDueDate)
    val selectedAppliance = appliances.firstOrNull { it.id == applianceId }
    val parsedInterval = interval.toIntOrNull()
    val titleError = submitted && title.isBlank()
    val applianceError = submitted && selectedAppliance == null
    val intervalError = submitted && (parsedInterval == null || parsedInterval <= 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onCancel) { Text("← Назад") }
        Text(
            if (task == null) "Новая задача" else "Редактирование задачи",
            style = MaterialTheme.typography.headlineMedium
        )
        if (appliances.isEmpty()) {
            Text("Сначала добавьте устройство во вкладке «Моя техника».")
            OutlinedButton(onClick = onCancel) { Text("Отмена") }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = { applianceMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedAppliance?.name ?: "Выберите устройство *")
                }
                DropdownMenu(
                    expanded = applianceMenuExpanded,
                    onDismissRequest = { applianceMenuExpanded = false }
                ) {
                    appliances.forEach { appliance ->
                        DropdownMenuItem(
                            text = { Text(appliance.name) },
                            onClick = {
                                applianceId = appliance.id
                                applianceMenuExpanded = false
                                onErrorDismissed()
                            }
                        )
                    }
                }
                if (applianceError) {
                    Text("Выберите устройство.", color = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; onErrorDismissed() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название обслуживания *") },
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Введите название задачи.") }
                } else null
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; onErrorDismissed() },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Описание") },
                minLines = 2
            )
            OutlinedTextField(
                value = interval,
                onValueChange = { value ->
                    interval = value.filter(Char::isDigit)
                    onErrorDismissed()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Периодичность, дней *") },
                singleLine = true,
                isError = intervalError,
                supportingText = if (intervalError) {
                    { Text("Укажите положительное число дней.") }
                } else null
            )
            OutlinedButton(
                onClick = {
                    datePickerState.selectedDateMillis = nextDueDate
                    datePickerOpen = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Следующее обслуживание: ${formatUtcDate(nextDueDate)}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Задача активна")
                Switch(checked = isActive, onCheckedChange = { isActive = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Напоминать об обслуживании")
                    Text(
                        "Уведомление в день обслуживания около 09:00.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        notificationsEnabled = enabled
                        if (enabled && !notificationPermissionGranted) {
                            permissionExplanationOpen = true
                        }
                    }
                )
            }
            if (notificationsEnabled && !notificationPermissionGranted) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Разрешите уведомления в Android, чтобы получать напоминания.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = { permissionExplanationOpen = true }) {
                        Text("Предоставить разрешение")
                    }
                }
            }
            state.operationError?.let {
                ApplianceErrorBanner(it, onDismiss = onErrorDismissed)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving
                ) { Text("Отмена") }
                Button(
                    onClick = {
                        submitted = true
                        val validInterval = interval.toIntOrNull()
                        if (selectedAppliance == null || title.isBlank() ||
                            validInterval == null || validInterval <= 0 || state.isSaving
                        ) return@Button
                        onSave(
                            MaintenanceTask(
                                id = task?.id ?: 0L,
                                applianceId = selectedAppliance.id,
                                title = title.trim(),
                                description = description.trim().ifEmpty { null },
                                intervalDays = validInterval,
                                nextDueDate = nextDueDate,
                                isActive = isActive,
                                notificationsEnabled = notificationsEnabled
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Сохранить")
                }
            }
        }
    }

    if (datePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { nextDueDate = it }
                    datePickerOpen = false
                }) { Text("Выбрать") }
            },
            dismissButton = {
                TextButton(onClick = { datePickerOpen = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (permissionExplanationOpen) {
        AlertDialog(
            onDismissRequest = { permissionExplanationOpen = false },
            title = { Text("Напоминания об обслуживании") },
            text = {
                Text(
                    "Разрешите уведомления, чтобы HomeCareTracker напомнил вам " +
                        "об обслуживании техники в выбранный день."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    permissionExplanationOpen = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("Продолжить") }
            },
            dismissButton = {
                TextButton(onClick = { permissionExplanationOpen = false }) {
                    Text("Не сейчас")
                }
            }
        )
    }
}

private fun hasNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
}
