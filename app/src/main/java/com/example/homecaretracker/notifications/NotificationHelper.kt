package com.example.homecaretracker.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.homecaretracker.MainActivity
import com.example.homecaretracker.R
import com.example.homecaretracker.navigation.MaintenanceRoutes
import com.example.homecaretracker.util.formatUtcDate

object NotificationHelper {
    private const val CHANNEL_ID = "maintenance_reminders"
    private const val CHANNEL_NAME = "Напоминания об обслуживании"
    const val TASK_ID_EXTRA = "com.example.homecaretracker.EXTRA_MAINTENANCE_TASK_ID"
    const val TASK_NOTIFICATION_ACTION = "com.example.homecaretracker.OPEN_MAINTENANCE_TASK"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Напоминает о плановом обслуживании бытовой техники."
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun hasPermission(context: Context): Boolean =
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun showReminder(
        context: Context,
        taskId: Long,
        taskTitle: String,
        applianceName: String,
        dueDate: Long
    ) {
        if (!hasPermission(context)) return
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = TASK_NOTIFICATION_ACTION
            putExtra(TASK_ID_EXTRA, taskId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Пора обслужить технику")
            .setContentText("$taskTitle · $applianceName")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$taskTitle — $applianceName\nДата обслуживания: ${formatUtcDate(dueDate)}"
                )
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
    }
}
