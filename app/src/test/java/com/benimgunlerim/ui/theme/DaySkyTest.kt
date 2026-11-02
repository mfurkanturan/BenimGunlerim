package com.benimgunlerim.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class DaySkyTest {
    @Test
    fun dayPhaseFollowsTimeOfDayBoundaries() {
        assertEquals(DayPhase.Gece, dayPhaseFor(LocalTime.of(4, 59)))
        assertEquals(DayPhase.Sabah, dayPhaseFor(LocalTime.of(5, 0)))
        assertEquals(DayPhase.Sabah, dayPhaseFor(LocalTime.of(10, 59)))
        assertEquals(DayPhase.Ogle, dayPhaseFor(LocalTime.of(11, 0)))
        assertEquals(DayPhase.Ogle, dayPhaseFor(LocalTime.of(16, 29)))
        assertEquals(DayPhase.Aksam, dayPhaseFor(LocalTime.of(16, 30)))
        assertEquals(DayPhase.Aksam, dayPhaseFor(LocalTime.of(20, 29)))
        assertEquals(DayPhase.Gece, dayPhaseFor(LocalTime.of(20, 30)))
        assertEquals(DayPhase.Gece, dayPhaseFor(LocalTime.MIDNIGHT))
    }

    @Test
    fun onlyNightPaletteShowsMoon() {
        assertTrue(skyPaletteFor(DayPhase.Gece).isMoon)
        DayPhase.entries.filter { it != DayPhase.Gece }.forEach { assertFalse(skyPaletteFor(it).isMoon) }
    }
}
