package com.benimgunlerim.notifications

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import java.time.DayOfWeek

/** Rutin hatırlatması gösterilsin mi: bugün rutinin günü mü ve bugün henüz yapılmamış mı. */
internal object RoutineReminderGate {
    fun shouldRemind(
        routineId: String,
        targetDays: String,
        today: DayOfWeek,
        todayLogs: List<CompletionLogEntity>,
    ): Boolean {
        if (targetDays.isNotBlank() && today.name !in targetDays.split(",")) return false
        val doneToday = todayLogs.any {
            it.entityType == CompletionEntityType.ROUTINE.value &&
                it.entityId == routineId &&
                it.status == CompletionStatus.COMPLETED.value
        }
        return !doneToday
    }
}
