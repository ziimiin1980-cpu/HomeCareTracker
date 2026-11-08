package com.example.homecaretracker.ui.screens

import androidx.compose.runtime.Composable
import com.example.homecaretracker.viewmodel.ApplianceUiState

@Composable
fun AppliancesScreen(
    state: ApplianceUiState,
    onAddClick: () -> Unit,
    onApplianceClick: (Long) -> Unit,
    onRetry: () -> Unit
) {
    ApplianceListScreen(
        state = state,
        onAddClick = onAddClick,
        onApplianceClick = onApplianceClick,
        onRetry = onRetry
    )
}
