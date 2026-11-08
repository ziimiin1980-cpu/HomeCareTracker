package com.example.homecaretracker.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homecaretracker.data.database.HomeCareDatabase
import com.example.homecaretracker.data.repository.ApplianceRepository
import com.example.homecaretracker.data.repository.MaintenanceTaskRepository
import com.example.homecaretracker.util.toUtcDate
import kotlinx.coroutines.CancellationException
import java.time.Instant
import java.time.ZoneId

class MaintenanceNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(TASK_ID, -1L)
        if (taskId <= 0L) return Result.failure()
        return try {
            val database = HomeCareDatabase.getDatabase(applicationContext)
            val taskRepository = MaintenanceTaskRepository(database.maintenanceTaskDao())
            val applianceRepository = ApplianceRepository(database.applianceDao())
            val task = taskRepository.getById(taskId) ?: return Result.success()
            if (!task.isActive || !task.notificationsEnabled) return Result.success()

            val zoneId = ZoneId.systemDefault()
            val today = Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(zoneId)
                .toLocalDate()
            val dueDate = task.nextDueDate.toUtcDate()
            if (dueDate != today) return Result.success()

            val appliance = applianceRepository.getById(task.applianceId)
                ?: return Result.success()
            NotificationHelper.showReminder(
                context = applicationContext,
                taskId = task.id,
                taskTitle = task.title,
                applianceName = appliance.name,
                dueDate = task.nextDueDate
            )
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val TASK_ID = "maintenance_task_id"
    }
}
