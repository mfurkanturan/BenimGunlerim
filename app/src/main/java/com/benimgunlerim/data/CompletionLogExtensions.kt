package com.benimgunlerim.data

import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.domain.model.CompletionStatus
import java.time.DayOfWeek
import java.time.LocalDate

fun List<CompletionLogEntity>.currentStreak(today: LocalDate): Int {
    val completedDates = filter { it.status == CompletionStatus.COMPLETED.value }.mapNotNull {
        runCatching { LocalDate.parse(it.date) }.getOrNull()
    }.toSet()

    return if (today in completedDates) {
        var streak = 0
        var cursor = today
        while (cursor in completedDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        streak
    } else if (today.minusDays(1) in completedDates) {
        var streak = 0
        var cursor = today.minusDays(1)
        while (cursor in completedDates) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        streak
    } else {
        0
    }
}

/**
 * Tek bir rutinin/görevin zinciri. [scheduledDays] verilirse sadece o günler sayılır:
 * rutinin günü olmayan günler zinciri kırmaz, kaçırılan planlı gün kırar.
 * Bugün planlıysa ama henüz yapılmadıysa zincir dünden sayılır (gün daha bitmedi).
 */
fun List<CompletionLogEntity>.currentStreakForEntity(
    entityType: String,
    entityId: String,
    today: LocalDate,
    scheduledDays: Set<DayOfWeek> = emptySet(),
): Int {
    val mine = filter { it.entityType == entityType && it.entityId == entityId }
    if (scheduledDays.isEmpty() || scheduledDays.size == DayOfWeek.entries.size) return mine.currentStreak(today)
    val done = mine.filter { it.status == CompletionStatus.COMPLETED.value }
        .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
        .toSet()
    var cursor = if (today.dayOfWeek in scheduledDays && today !in done) today.minusDays(1) else today
    var streak = 0
    repeat(MaxStreakLookbackDays) {
        when {
            cursor.dayOfWeek !in scheduledDays -> Unit
            cursor in done -> streak++
            else -> return streak
        }
        cursor = cursor.minusDays(1)
    }
    return streak
}

private const val MaxStreakLookbackDays = 400
