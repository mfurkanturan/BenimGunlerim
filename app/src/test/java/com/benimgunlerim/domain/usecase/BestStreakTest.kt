package com.benimgunlerim.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class BestStreakTest {
    private fun day(date: String, score: Int) = ProgressDayData(
        date = date, dailyScore = score, completionRate = score / 100f, mood = null, energyLevel = null, note = null,
    )

    @Test
    fun lowButNonZeroDaysCountTowardBestStreak() {
        // Üç gün art arda %25 — "küçük adım yeter": seri 3.
        val days = listOf(day("2026-11-01", 25), day("2026-11-02", 25), day("2026-11-03", 25), day("2026-11-04", 0))
        assertEquals(3, days.bestStreak())
    }

    @Test
    fun zeroDayBreaksStreak() {
        val days = listOf(day("2026-11-01", 100), day("2026-11-02", 0), day("2026-11-03", 40), day("2026-11-04", 10))
        assertEquals(2, days.bestStreak())
    }
}
