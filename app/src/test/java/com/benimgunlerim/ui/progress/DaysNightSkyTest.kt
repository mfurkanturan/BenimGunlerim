package com.benimgunlerim.ui.progress

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DaysNightSkyTest {
    private val month = YearMonth.of(2026, 11)

    @Test
    fun longestRunCountsConsecutiveLitDaysInMonth() {
        val lit = setOf(1, 2, 3, 5, 6).map { month.atDay(it) }.toSet()
        assertEquals(3, longestRunIn(lit, month))
    }

    @Test
    fun longestRunIgnoresOtherMonthsAndEmpty() {
        assertEquals(0, longestRunIn(emptySet(), month))
        assertEquals(1, longestRunIn(setOf(LocalDate.of(2026, 10, 31), month.atDay(1)), month))
    }
}
