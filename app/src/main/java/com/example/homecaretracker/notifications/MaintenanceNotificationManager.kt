package com.example.homecaretracker.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.homecaretracker.data.entity.MaintenanceTask
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MaintenanceNotificationManager(
    context: Context,
    private val workManager: WorkManager = WorkManager.getInstance(context.applicationContext),
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val zoneId: () -> ZoneId = ZoneId::systemDefault
) {
    fun schedule(task: MaintenanceTask) {
        val uniqueName = uniqueWorkName(task.id)
        if (!task.isActive || !task.notificationsEnabled) {
            cancel(task.id)
            return
        }
        val delayMillis = MaintenanceReminderSchedule.delayMillis(
            nextDueDate = task.nextDueDate,
            nowMillis = nowMillis(),
            zoneId = zoneId()
        )
        if (delayMillis == null) {
            cancel(task.id)
            return
        }
        val request = OneTimeWorkRequestBuilder<MaintenanceNotificationWorker>()
            .setInputData(Data.Builder().putLong(MaintenanceNotificationWorker.TASK_ID, task.id).build())
            .addTag("maintenance-task-${task.id}")
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(taskId: Long) {
        workManager.cancelUniqueWork(uniqueWorkName(taskId))
    }

    companion object {
        fun uniqueWorkName(taskId: Long): String = "maintenance-reminder-$taskId"
    }
}
