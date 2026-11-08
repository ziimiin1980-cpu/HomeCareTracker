package com.example.homecaretracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

fun Long.toUtcDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneOffset.UTC)
    .toLocalDate()

fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

fun nextDueDateAfterCompletion(
    scheduledDate: Long,
    completedAt: Long,
    intervalDays: Int
): Long {
    val today = completedAt.toUtcDate()
    val scheduled = scheduledDate.toUtcDate()
    return (if (scheduled.isBefore(today)) today else scheduled)
        .plusDays(intervalDays.toLong())
        .toUtcMillis()
}
