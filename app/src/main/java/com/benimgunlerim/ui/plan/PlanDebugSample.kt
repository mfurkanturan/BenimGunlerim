package com.benimgunlerim.ui.plan

import java.time.LocalDate

/**
 * Yalnızca debug: Plan'ın dolu hâlini kullanıcının gerçek verisine (ve bulut yedeğine) dokunmadan
 * görmek için örnek görevler (adb extra "debug_sample_plan"). Metinler arayüz değil örnek veri olduğu
 * için strings.xml'de değil.
 */
internal fun PlanUiState.withDebugSample(): PlanUiState {
    val day = selectedDate
    fun task(id: String, title: String, time: String?, category: String?, priority: Int, done: Boolean = false, note: String? = null, date: LocalDate = day) =
        PlanTaskUi(
            id = "debug-$id",
            title = title,
            note = note,
            plannedDate = date.toString(),
            startTime = time,
            category = category,
            priority = priority,
            reminderTime = time,
            isCompleted = done,
        )
    return copy(
        tasksForDay = listOf(
            task("1", "Sabah koşusu", "07:30", "Sağlık", priority = 2, done = true),
            task("2", "Proje sunumunu bitir", "10:00", "İş", priority = 1, note = "Son iki slayt kaldı"),
            task("3", "Annemi ara", "13:00", "Aile", priority = 2),
            task("4", "Market alışverişi", "18:30", "Ev", priority = 3),
            task("5", "Kitaptan 20 sayfa oku", null, "Kişisel", priority = 3),
        ),
        overdueTasks = listOf(
            task("6", "Faturayı öde", null, "Ev", priority = 1, date = day.minusDays(2)),
            task("7", "Diş randevusu al", null, "Sağlık", priority = 2, date = day.minusDays(1)),
        ),
        weeklyTaskCounts = (0L..6L).associate { weekStart.plusDays(it) to listOf(3, 5, 2, 4, 1, 0, 2)[it.toInt()] },
    )
}
