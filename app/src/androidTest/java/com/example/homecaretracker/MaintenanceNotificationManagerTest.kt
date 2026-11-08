package com.example.homecaretracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.homecaretracker.data.entity.MaintenanceTask
import com.example.homecaretracker.notifications.MaintenanceNotificationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MaintenanceNotificationManagerTest {
    @Test
    fun schedulingReplacesTheUniqueWorkAndDisablingCancelsIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val workManager = WorkManager.getInstance(context)
        val notificationManager = MaintenanceNotificationManager(context)
        val taskId = System.currentTimeMillis()
        val task = MaintenanceTask(
            id = taskId,
            applianceId = 1,
            title = "Очистить фильтр",
            intervalDays = 30,
            nextDueDate = LocalDate.now(ZoneOffset.UTC).plusDays(3)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
            notificationsEnabled = true
        )
        val uniqueName = MaintenanceNotificationManager.uniqueWorkName(taskId)
        try {
            notificationManager.schedule(task)
            var work = workManager.getWorkInfosForUniqueWork(uniqueName)
                .get(10, TimeUnit.SECONDS)
            assertEquals(1, work.size)
            assertEquals(WorkInfo.State.ENQUEUED, work.single().state)
            assertTrue(work.single().tags.contains("maintenance-task-$taskId"))

            notificationManager.schedule(task.copy(title = "Проверить фильтр"))
            work = workManager.getWorkInfosForUniqueWork(uniqueName)
                .get(10, TimeUnit.SECONDS)
            assertEquals(1, work.size)
            assertEquals(WorkInfo.State.ENQUEUED, work.single().state)

            workManager.cancelUniqueWork(uniqueName).result.get(10, TimeUnit.SECONDS)
            work = workManager.getWorkInfosForUniqueWork(uniqueName)
                .get(10, TimeUnit.SECONDS)
            assertTrue(work.all { it.state == WorkInfo.State.CANCELLED })
        } finally {
            workManager.cancelUniqueWork(uniqueName).result.get(10, TimeUnit.SECONDS)
        }
    }
}
