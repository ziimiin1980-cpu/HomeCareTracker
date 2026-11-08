package com.example.homecaretracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.homecaretracker.data.database.HomeCareDatabase
import com.example.homecaretracker.data.repository.ApplianceRepository
import com.example.homecaretracker.data.repository.MaintenanceHistoryRepository
import com.example.homecaretracker.data.repository.MaintenanceTaskRepository
import com.example.homecaretracker.notifications.MaintenanceNotificationManager
import com.example.homecaretracker.ui.components.HomeCareBottomBar
import com.example.homecaretracker.ui.screens.ApplianceDetailScreen
import com.example.homecaretracker.ui.screens.ApplianceFormScreen
import com.example.homecaretracker.ui.screens.ApplianceMissingScreen
import com.example.homecaretracker.ui.screens.AppliancesScreen
import com.example.homecaretracker.ui.screens.MaintenanceTaskDetailScreen
import com.example.homecaretracker.ui.screens.MaintenanceTaskFormScreen
import com.example.homecaretracker.ui.screens.HistoryScreen
import com.example.homecaretracker.ui.screens.HomeScreen
import com.example.homecaretracker.ui.screens.MaintenanceScreen
import com.example.homecaretracker.ui.theme.AppThemeMode
import com.example.homecaretracker.viewmodel.ApplianceViewModel
import com.example.homecaretracker.viewmodel.HomeDashboardViewModel
import com.example.homecaretracker.viewmodel.HistoryViewModel
import com.example.homecaretracker.viewmodel.MaintenanceViewModel

@Composable
fun HomeCareApp(
    notificationTaskId: Long? = null,
    onNotificationTaskHandled: () -> Unit = {},
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeChanged: (AppThemeMode) -> Unit = {}
) {
    val navController = rememberNavController()
    val applicationContext = LocalContext.current.applicationContext
    val database = remember(applicationContext) {
        HomeCareDatabase.getDatabase(applicationContext)
    }
    val applianceRepository = remember(database) {
        ApplianceRepository(database.applianceDao())
    }
    val taskRepository = remember(database) {
        MaintenanceTaskRepository(database.maintenanceTaskDao())
    }
    val historyRepository = remember(database) {
        MaintenanceHistoryRepository(database.maintenanceHistoryDao())
    }
    val notificationManager = remember(applicationContext) {
        MaintenanceNotificationManager(applicationContext)
    }
    val applianceViewModel: ApplianceViewModel = viewModel(
        factory = remember(applianceRepository) {
            ApplianceViewModel.Factory(applianceRepository)
        }
    )
    val applianceState by applianceViewModel.uiState.collectAsState()
    val maintenanceViewModel: MaintenanceViewModel = viewModel(
        factory = remember(taskRepository, applianceRepository, notificationManager) {
            MaintenanceViewModel.Factory(
                taskRepository,
                applianceRepository,
                notificationManager
            )
        }
    )
    val maintenanceState by maintenanceViewModel.uiState.collectAsState()
    val historyViewModel: HistoryViewModel = viewModel(
        factory = remember(historyRepository) { HistoryViewModel.Factory(historyRepository) }
    )
    val historyState by historyViewModel.uiState.collectAsState()
    val dashboardViewModel: HomeDashboardViewModel = viewModel(
        factory = remember(applianceRepository, taskRepository, historyRepository) {
            HomeDashboardViewModel.Factory(
                applianceRepository,
                taskRepository,
                historyRepository
            )
        }
    )
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedDestination = HomeCareDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { navDestination ->
            navDestination.route == destination.route ||
                (destination == HomeCareDestination.APPLIANCES &&
                navDestination.route?.startsWith("appliances/") == true) ||
            (destination == HomeCareDestination.MAINTENANCE &&
                navDestination.route?.startsWith("maintenance/") == true)
        } == true
    } ?: HomeCareDestination.HOME

    LaunchedEffect(notificationTaskId) {
        notificationTaskId?.let { taskId ->
            navController.navigate(MaintenanceRoutes.detail(taskId)) {
                launchSingleTop = true
            }
            onNotificationTaskHandled()
        }
    }

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
            composable(HomeCareDestination.HOME.route) {
                HomeScreen(
                    state = dashboardState,
                    onAddAppliance = {
                        applianceViewModel.clearOperationError()
                        navController.navigate(ApplianceRoutes.CREATE)
                    },
                    onAddTask = {
                        maintenanceViewModel.clearMessages()
                        navController.navigate(MaintenanceRoutes.CREATE)
                    },
                    onOpenMaintenance = {
                        navController.navigate(HomeCareDestination.MAINTENANCE.route)
                    },
                    onOpenHistory = {
                        navController.navigate(HomeCareDestination.HISTORY.route)
                    },
                    onRetry = dashboardViewModel::retryLoading,
                    themeMode = themeMode,
                    onThemeModeChanged = onThemeModeChanged
                )
            }
            composable(HomeCareDestination.APPLIANCES.route) {
                AppliancesScreen(
                    state = applianceState,
                    onAddClick = {
                        applianceViewModel.clearOperationError()
                        navController.navigate(ApplianceRoutes.CREATE)
                    },
                    onApplianceClick = { applianceId ->
                        applianceViewModel.clearOperationError()
                        navController.navigate(ApplianceRoutes.detail(applianceId))
                    },
                    onRetry = applianceViewModel::retryLoading
                )
            }
            composable(ApplianceRoutes.CREATE) {
                ApplianceFormScreen(
                    appliance = null,
                    state = applianceState,
                    onSave = { appliance ->
                        applianceViewModel.saveAppliance(appliance) {
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onErrorDismissed = applianceViewModel::clearOperationError
                )
            }
            composable(
                route = ApplianceRoutes.DETAIL,
                arguments = listOf(navArgument("applianceId") { type = NavType.LongType })
            ) { entry ->
                val applianceId = entry.arguments?.getLong("applianceId")
                val appliance = applianceState.appliances.firstOrNull {
                    it.id == applianceId
                }
                ApplianceDetailScreen(
                    appliance = appliance,
                    state = applianceState,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        applianceViewModel.clearOperationError()
                        appliance?.let {
                            navController.navigate(ApplianceRoutes.edit(it.id))
                        }
                    },
                    onDelete = {
                        appliance?.let {
                            applianceViewModel.deleteAppliance(it.id) {
                                navController.popBackStack()
                            }
                        }
                    },
                    onErrorDismissed = applianceViewModel::clearOperationError
                )
            }
            composable(
                route = ApplianceRoutes.EDIT,
                arguments = listOf(navArgument("applianceId") { type = NavType.LongType })
            ) { entry ->
                val applianceId = entry.arguments?.getLong("applianceId")
                val appliance = applianceState.appliances.firstOrNull {
                    it.id == applianceId
                }
                if (appliance == null) {
                    ApplianceMissingScreen(
                        isLoading = applianceState.isLoading,
                        error = applianceState.loadError,
                        onBack = { navController.popBackStack() },
                        onRetry = applianceViewModel::retryLoading
                    )
                } else {
                    ApplianceFormScreen(
                        appliance = appliance,
                        state = applianceState,
                        onSave = { updatedAppliance ->
                            applianceViewModel.saveAppliance(updatedAppliance) {
                                navController.popBackStack()
                            }
                        },
                        onCancel = { navController.popBackStack() },
                        onErrorDismissed = applianceViewModel::clearOperationError
                    )
                }
            }
            composable(HomeCareDestination.MAINTENANCE.route) {
                MaintenanceScreen(
                    state = maintenanceState,
                    onAddClick = {
                        maintenanceViewModel.clearMessages()
                        navController.navigate(MaintenanceRoutes.CREATE)
                    },
                    onTaskClick = { navController.navigate(MaintenanceRoutes.detail(it)) },
                    onToggle = maintenanceViewModel::setTaskActive,
                    onComplete = maintenanceViewModel::completeTask,
                    onRetry = maintenanceViewModel::retryLoading,
                    onErrorDismissed = maintenanceViewModel::clearMessages
                )
            }
            composable(MaintenanceRoutes.CREATE) {
                MaintenanceTaskFormScreen(
                    task = null,
                    appliances = maintenanceState.appliances,
                    state = maintenanceState,
                    onSave = { task ->
                        maintenanceViewModel.saveTask(task) { navController.popBackStack() }
                    },
                    onCancel = { navController.popBackStack() },
                    onErrorDismissed = maintenanceViewModel::clearMessages
                )
            }
            composable(
                route = MaintenanceRoutes.DETAIL,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { entry ->
                val taskId = entry.arguments?.getLong("taskId")
                val task = maintenanceState.tasks.firstOrNull { it.id == taskId }
                MaintenanceTaskDetailScreen(
                    task = task,
                    applianceName = maintenanceState.appliances
                        .firstOrNull { it.id == task?.applianceId }?.name,
                    state = maintenanceState,
                    onBack = { navController.popBackStack() },
                    onEdit = { task?.let { navController.navigate(MaintenanceRoutes.edit(it.id)) } },
                    onToggle = { enabled ->
                        task?.let { maintenanceViewModel.setTaskActive(it, enabled) }
                    },
                    onComplete = { task?.let { maintenanceViewModel.completeTask(it.id) } },
                    onDelete = {
                        task?.let {
                            maintenanceViewModel.deleteTask(it.id) { navController.popBackStack() }
                        }
                    },
                    onErrorDismissed = maintenanceViewModel::clearMessages
                )
            }
            composable(
                route = MaintenanceRoutes.EDIT,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { entry ->
                val taskId = entry.arguments?.getLong("taskId")
                val task = maintenanceState.tasks.firstOrNull { it.id == taskId }
                MaintenanceTaskFormScreen(
                    task = task,
                    appliances = maintenanceState.appliances,
                    state = maintenanceState,
                    onSave = { updatedTask ->
                        maintenanceViewModel.saveTask(updatedTask) { navController.popBackStack() }
                    },
                    onCancel = { navController.popBackStack() },
                    onErrorDismissed = maintenanceViewModel::clearMessages
                )
            }
            composable(HomeCareDestination.HISTORY.route) {
                HistoryScreen(
                    state = historyState,
                    onRetry = historyViewModel::retryLoading
                )
            }
        }
    }
}
