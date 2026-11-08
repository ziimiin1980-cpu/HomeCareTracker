package com.example.homecaretracker.notifications

import com.example.homecaretracker.util.toUtcDate
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId

object MaintenanceReminderSchedule {
    private val REMINDER_TIME = LocalTime.of(9, 0)
    private val MISSED_REMINDER_DELAY = Duration.ofMinutes(15)

    fun delayMillis(
        nextDueDate: Long,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        val dueDate = nextDueDate.toUtcDate()
        if (dueDate.isBefore(today)) return null

        val now = java.time.Instant.ofEpochMilli(nowMillis)
        val reminderAt = dueDate.atTime(REMINDER_TIME).atZone(zoneId).toInstant()
        val target = if (dueDate == today && !reminderAt.isAfter(now)) {
            now.plus(MISSED_REMINDER_DELAY)
        } else {
            reminderAt
        }
        return Duration.between(now, target).toMillis().coerceAtLeast(0L)
    }
}
