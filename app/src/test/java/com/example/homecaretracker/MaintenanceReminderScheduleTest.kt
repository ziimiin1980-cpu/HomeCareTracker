package com.example.homecaretracker

import com.example.homecaretracker.notifications.MaintenanceReminderSchedule
import com.example.homecaretracker.util.toUtcMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MaintenanceReminderScheduleTest {
    private val zone = ZoneId.of("Europe/Moscow")

    @Test
    fun schedulesAtNineLocalTime() {
        val now = LocalDateTime.of(2026, 11, 8, 8, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val dueDate = LocalDate.of(2026, 11, 8).toUtcMillis()

        assertEquals(
            60 * 60 * 1000L,
            MaintenanceReminderSchedule.delayMillis(dueDate, now, zone)
        )
    }

    @Test
    fun schedulesShortlyAfterNineWhenTheTimeHasPassed() {
        val now = LocalDateTime.of(2026, 11, 8, 9, 5)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val dueDate = LocalDate.of(2026, 11, 8).toUtcMillis()

        assertEquals(
            15 * 60 * 1000L,
            MaintenanceReminderSchedule.delayMillis(dueDate, now, zone)
        )
    }

    @Test
    fun overdueTaskDoesNotScheduleAnotherReminder() {
        val now = LocalDateTime.of(2026, 11, 8, 8, 0)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val dueDate = LocalDate.of(2026, 11, 7).toUtcMillis()

        assertNull(MaintenanceReminderSchedule.delayMillis(dueDate, now, zone))
    }
}
