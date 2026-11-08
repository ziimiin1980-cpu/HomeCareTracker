package com.example.homecaretracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.ui.components.ApplianceErrorBanner

@Composable
fun ApplianceMissingScreen(
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Моя техника")
        }
        when {
            isLoading -> CircularProgressIndicator()
            error != null -> {
                ApplianceErrorBanner(error)
                OutlinedButton(onClick = onRetry) {
                    Text("Повторить")
                }
            }
            else -> Text(
                text = "Устройство не найдено.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
