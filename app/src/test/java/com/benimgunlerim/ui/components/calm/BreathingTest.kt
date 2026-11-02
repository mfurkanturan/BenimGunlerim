package com.benimgunlerim.ui.components.calm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BreathingTest {
    @Test
    fun labelAndCircleShareOneCycle() {
        assertTrue(isInhaling(0L))
        assertTrue(isInhaling(3_999L))
        assertFalse(isInhaling(4_000L))
        assertFalse(isInhaling(9_999L))
        assertTrue(isInhaling(10_000L))
    }

    @Test
    fun circleGrowsWhileInhalingAndShrinksWhileExhaling() {
        assertTrue(breathScale(1_000L) < breathScale(3_000L))
        assertEquals(1f, breathScale(4_000L), 0.001f)
        assertTrue(breathScale(6_000L) > breathScale(9_000L))
        assertEquals(breathScale(0L), breathScale(10_000L), 0.001f)
    }
}
