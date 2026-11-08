package com.example.homecaretracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.homecaretracker.ui.components.HomeCareBottomBar
import com.example.homecaretracker.ui.screens.AppliancesScreen
import com.example.homecaretracker.ui.screens.HistoryScreen
import com.example.homecaretracker.ui.screens.HomeScreen
import com.example.homecaretracker.ui.screens.MaintenanceScreen

@Composable
fun HomeCareApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedDestination = HomeCareDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    } ?: HomeCareDestination.HOME

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HomeCareBottomBar(selectedDestination = selectedDestination) { destination ->
                navController.navigate(
                    destination.route,
                    navOptions {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                )
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeCareDestination.HOME.route,
            modifier = Modifier.padding(contentPadding)
        ) {
            composable(HomeCareDestination.HOME.route) { HomeScreen() }
            composable(HomeCareDestination.APPLIANCES.route) { AppliancesScreen() }
            composable(HomeCareDestination.MAINTENANCE.route) { MaintenanceScreen() }
            composable(HomeCareDestination.HISTORY.route) { HistoryScreen() }
        }
    }
}
