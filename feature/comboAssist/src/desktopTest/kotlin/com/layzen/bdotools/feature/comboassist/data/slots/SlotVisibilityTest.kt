package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SlotVisibilityTest {
    @Test
    fun hidesAfterSeveralChecksWithoutRecognizedSlotsAndShowsAtOnce() {
        val visibility = SlotVisibility()
        repeat(3) { visibility.onCheck(0) }
        assertTrue(visibility.shown) // нескольких проверок мало
        assertTrue(visibility.onCheck(1)) // один узнанный слот — ещё не видно
        assertFalse(visibility.shown)
        assertTrue(visibility.onCheck(3))
        assertTrue(visibility.shown)
    }

    @Test
    fun slotIsRecognizedOnlyWhenOwnIconIsClearlyCloser() {
        assertTrue(SlotVisibility.recognized(30.0, listOf(55.0, 60.0)))
        assertFalse(SlotVisibility.recognized(40.0, listOf(44.0, 60.0))) // фон: своя не лучше чужих
        assertFalse(SlotVisibility.recognized(null, listOf(44.0)))
    }
}
