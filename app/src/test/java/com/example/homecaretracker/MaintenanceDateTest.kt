package com.example.homecaretracker

import com.example.homecaretracker.util.nextDueDateAfterCompletion
import com.example.homecaretracker.util.toUtcMillis
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MaintenanceDateTest {
    @Test
    fun overdueTaskIsRescheduledFromCompletionDate() {
        val completedAt = LocalDate.of(2026, 11, 8).toUtcMillis()
        val overdueDate = LocalDate.of(2026, 10, 1).toUtcMillis()

        assertEquals(
            LocalDate.of(2026, 11, 18).toUtcMillis(),
            nextDueDateAfterCompletion(overdueDate, completedAt, 10)
        )
    }

    @Test
    fun futureScheduleAdvancesFromItsScheduledDate() {
        val completedAt = LocalDate.of(2026, 11, 8).toUtcMillis()
        val scheduledDate = LocalDate.of(2026, 11, 20).toUtcMillis()

        assertEquals(
            LocalDate.of(2026, 12, 20).toUtcMillis(),
            nextDueDateAfterCompletion(scheduledDate, completedAt, 30)
        )
    }
}
