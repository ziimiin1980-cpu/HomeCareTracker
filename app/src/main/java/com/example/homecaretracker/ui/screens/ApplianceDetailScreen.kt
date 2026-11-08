package com.example.homecaretracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.data.entity.Appliance
import com.example.homecaretracker.ui.components.ApplianceErrorBanner
import com.example.homecaretracker.viewmodel.ApplianceUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ApplianceDetailScreen(
    appliance: Appliance?,
    state: ApplianceUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onErrorDismissed: () -> Unit
) {
    var showDeleteConfirmation by remember(appliance?.id) {
        mutableStateOf(false)
    }

    if (appliance == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBack) { Text("← Моя техника") }
            when {
                state.isLoading -> CircularProgressIndicator()
                state.loadError != null -> ApplianceErrorBanner(state.loadError)
                else -> Text("Устройство не найдено.")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Моя техника")
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = appliance.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = appliance.category,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ApplianceDetailRow("Производитель", appliance.manufacturer)
                ApplianceDetailRow("Модель", appliance.model)
                ApplianceDetailRow(
                    "Дата покупки",
                    appliance.purchaseDate?.let(::formatPurchaseDate)
                )
                ApplianceDetailRow("Примечание", appliance.notes)
            }
        }

        state.operationError?.let { error ->
            ApplianceErrorBanner(error, onDismiss = onErrorDismissed)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                Text("Редактировать")
            }
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                Text("Удалить устройство")
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isSaving) showDeleteConfirmation = false
            },
            title = { Text("Удалить устройство?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "«${appliance.name}» и связанные с ним задачи обслуживания " +
                            "и записи истории будут удалены без возможности восстановления."
                    )
                    state.operationError?.let { error ->
                        ApplianceErrorBanner(error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDelete,
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Подтвердить удаление")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    enabled = !state.isSaving
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ApplianceDetailRow(
    label: String,
    value: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value?.takeIf(String::isNotBlank) ?: "Не указано",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun formatPurchaseDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(timestamp))
}
