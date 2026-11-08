package com.example.homecaretracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.homecaretracker.navigation.HomeCareDestination

@Composable
fun HomeCareBottomBar(
    selectedDestination: HomeCareDestination,
    onDestinationSelected: (HomeCareDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        HomeCareDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destinationIcon(destination),
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(
                        destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

private fun destinationIcon(destination: HomeCareDestination): ImageVector =
    when (destination) {
        HomeCareDestination.HOME -> Icons.Filled.Home
        HomeCareDestination.APPLIANCES -> Icons.Filled.Kitchen
        HomeCareDestination.MAINTENANCE -> Icons.Filled.Build
        HomeCareDestination.HISTORY -> Icons.Filled.History
    }
