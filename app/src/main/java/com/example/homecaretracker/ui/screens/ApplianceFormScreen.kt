package com.example.homecaretracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.data.entity.Appliance
import com.example.homecaretracker.ui.components.ApplianceErrorBanner
import com.example.homecaretracker.viewmodel.ApplianceUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val applianceCategories = listOf(
    "Холодильник",
    "Стиральная машина",
    "Посудомоечная машина",
    "Кондиционер",
    "Телевизор",
    "Пылесос",
    "Духовой шкаф",
    "Другое"
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ApplianceFormScreen(
    appliance: Appliance?,
    state: ApplianceUiState,
    onSave: (Appliance) -> Unit,
    onCancel: () -> Unit,
    onErrorDismissed: () -> Unit
) {
    var name by rememberSaveable(appliance?.id) { mutableStateOf(appliance?.name.orEmpty()) }
    var category by rememberSaveable(appliance?.id) {
        mutableStateOf(appliance?.category.orEmpty())
    }
    var manufacturer by rememberSaveable(appliance?.id) {
        mutableStateOf(appliance?.manufacturer.orEmpty())
    }
    var model by rememberSaveable(appliance?.id) { mutableStateOf(appliance?.model.orEmpty()) }
    var notes by rememberSaveable(appliance?.id) { mutableStateOf(appliance?.notes.orEmpty()) }
    var purchaseDate by rememberSaveable(appliance?.id) {
        mutableStateOf(appliance?.purchaseDate)
    }
    var hasSubmitted by rememberSaveable(appliance?.id) { mutableStateOf(false) }
    var categoryMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var datePickerOpen by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = purchaseDate
    )
    val nameError = hasSubmitted && name.isBlank()
    val categoryError = hasSubmitted && category.isBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onCancel) {
            Text("← Назад")
        }
        Text(
            text = if (appliance == null) "Новое устройство" else "Редактирование",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Укажите основные сведения о приборе.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onErrorDismissed()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            isError = nameError,
            supportingText = if (nameError) {
                { Text("Введите название устройства.") }
            } else {
                null
            }
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { categoryMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.ifBlank { "Выбрать категорию *" },
                        color = if (categoryError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text("▾")
                }
            }
            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false },
                modifier = Modifier.widthIn(min = 260.dp)
            ) {
                applianceCategories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            onErrorDismissed()
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }
        if (categoryError) {
            Text(
                text = "Выберите категорию.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = manufacturer,
            onValueChange = {
                manufacturer = it
                onErrorDismissed()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Производитель") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                onErrorDismissed()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Модель") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedButton(
            onClick = {
                datePickerState.selectedDateMillis = purchaseDate
                datePickerOpen = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = purchaseDate?.let(::formatDate)
                    ?: "Указать дату покупки"
            )
        }
        if (purchaseDate != null) {
            TextButton(
                onClick = { purchaseDate = null },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Очистить дату")
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
                onErrorDismissed()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Примечание") },
            minLines = 3,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            )
        )

        state.operationError?.let { error ->
            ApplianceErrorBanner(
                message = error,
                onDismiss = onErrorDismissed
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                Text("Отмена")
            }
            Button(
                onClick = {
                    hasSubmitted = true
                    if (name.isBlank() || category.isBlank() || state.isSaving) {
                        return@Button
                    }
                    onSave(
                        Appliance(
                            id = appliance?.id ?: 0L,
                            name = name.trim(),
                            category = category,
                            manufacturer = manufacturer.trim().ifEmpty { null },
                            model = model.trim().ifEmpty { null },
                            purchaseDate = purchaseDate,
                            notes = notes.trim().ifEmpty { null }
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("Сохранить")
            }
        }
    }

    if (datePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        purchaseDate = datePickerState.selectedDateMillis
                        onErrorDismissed()
                        datePickerOpen = false
                    }
                ) {
                    Text("Выбрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerOpen = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(timestamp))
}
