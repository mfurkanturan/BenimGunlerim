package com.benimgunlerim.notifications

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class RoutineReminderGateTest {
    private fun log(id: String, status: CompletionStatus) = CompletionLogEntity(
        "log-$id", CompletionEntityType.ROUTINE.value, id, "2026-11-02", 1L, status.value, null,
    )

    @Test
    fun remindsOnScheduledDayWhenNotDone() {
        assertTrue(RoutineReminderGate.shouldRemind("r1", "MONDAY,WEDNESDAY", DayOfWeek.MONDAY, emptyList()))
    }

    @Test
    fun skipsWhenAlreadyDoneToday() {
        assertFalse(RoutineReminderGate.shouldRemind("r1", "", DayOfWeek.MONDAY, listOf(log("r1", CompletionStatus.COMPLETED))))
    }

    @Test
    fun skipsOnUnscheduledDay() {
        assertFalse(RoutineReminderGate.shouldRemind("r1", "MONDAY", DayOfWeek.TUESDAY, emptyList()))
    }

    @Test
    fun otherRoutinesCompletionDoesNotSilenceReminder() {
        assertTrue(RoutineReminderGate.shouldRemind("r1", "", DayOfWeek.MONDAY, listOf(log("r2", CompletionStatus.COMPLETED))))
    }
}
