package com.example.homecaretracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.example.homecaretracker.navigation.HomeCareDestination

@Composable
fun HomeCareBottomBar(
    selectedDestination: HomeCareDestination,
    onDestinationSelected: (HomeCareDestination) -> Unit
) {
    NavigationBar {
        HomeCareDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Text(
                        text = destination.icon,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 20.sp
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}
