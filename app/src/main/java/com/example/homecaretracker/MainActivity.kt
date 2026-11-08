package com.example.homecaretracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.homecaretracker.navigation.HomeCareApp
import com.example.homecaretracker.notifications.NotificationHelper
import com.example.homecaretracker.ui.theme.AppThemeMode
import com.example.homecaretracker.ui.theme.HomeCareTrackerTheme

class MainActivity : ComponentActivity() {
    private val notificationTaskId = MutableStateFlow<Long?>(null)
    private val themeMode = MutableStateFlow(AppThemeMode.SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        notificationTaskId.value = intent.taskIdFromNotification()
        themeMode.value = AppThemeMode.entries.firstOrNull {
            it.name == getPreferences(MODE_PRIVATE).getString(THEME_MODE_KEY, null)
        } ?: AppThemeMode.SYSTEM
        enableEdgeToEdge()
        setContent {
            val taskId by notificationTaskId.collectAsState()
            val selectedTheme by themeMode.collectAsState()
            HomeCareTrackerTheme(themeMode = selectedTheme) {
                HomeCareApp(
                    notificationTaskId = taskId,
                    onNotificationTaskHandled = { notificationTaskId.value = null },
                    themeMode = selectedTheme,
                    onThemeModeChanged = { mode ->
                        getPreferences(MODE_PRIVATE).edit()
                            .putString(THEME_MODE_KEY, mode.name)
                            .apply()
                        themeMode.value = mode
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationTaskId.value = intent.taskIdFromNotification()
    }

    private fun android.content.Intent.taskIdFromNotification(): Long? =
        if (action == NotificationHelper.TASK_NOTIFICATION_ACTION &&
            hasExtra(NotificationHelper.TASK_ID_EXTRA)
        ) {
            getLongExtra(NotificationHelper.TASK_ID_EXTRA, -1L).takeIf { it > 0L }
        } else {
            null
        }

    companion object {
        private const val THEME_MODE_KEY = "selected_theme_mode"
    }
}