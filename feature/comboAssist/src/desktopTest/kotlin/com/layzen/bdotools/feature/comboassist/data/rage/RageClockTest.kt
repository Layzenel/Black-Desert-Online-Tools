package com.layzen.bdotools.feature.comboassist.data.rage

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RageClockTest {
    @Test
    fun startKeepsRageForAMinuteWhateverIsOnScreen() {
        val clock = RageClock()
        assertTrue(clock.onCheck(0, started = false)) // первая проверка сообщает состояние
        assertFalse(clock.active)
        assertTrue(clock.onCheck(1_000, started = true))
        assertFalse(clock.onCheck(30_000, started = false))
        assertTrue(clock.active)
        assertTrue(clock.onCheck(60_000, started = false))
        assertFalse(clock.active)
    }

    @Test
    fun sameStartSeenAgainDoesNotProlong() {
        val clock = RageClock()
        clock.onCheck(0, started = true)
        clock.onCheck(800, started = true) // «59 с.» висит около секунды
        assertTrue(clock.onCheck(59_000, started = false))
        assertFalse(clock.active)
    }

    @Test
    fun nextRageStartsAgain() {
        val clock = RageClock()
        clock.onCheck(0, started = true)
        clock.onCheck(59_500, started = false)
        assertTrue(clock.onCheck(120_000, started = true))
        assertTrue(clock.active)
    }
}
