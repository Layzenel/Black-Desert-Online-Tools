package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SlotVisibilityTest {
    @Test
    fun slotNeverRecognizedStaysVisibleWithOthers() {
        // 2026-11-20: по своей иконке узнаются не все слоты — неузнаваемый не должен прятать тултип
        val visibility = SlotVisibility(4)
        repeat(30) { visibility.onCheck(listOf(true, true, false, false)) }
        assertEquals(emptySet(), visibility.hidden)
    }

    @Test
    fun slotRecognizedOnlySometimesDoesNotBlink() {
        // 2026-11-20: слоты 2 и 8 узнавались раз в 2 с и моргали щитами
        val visibility = SlotVisibility(3)
        repeat(300) { i -> visibility.onCheck(listOf(true, true, i % 20 == 0)); assertEquals(emptySet(), visibility.hidden) }
    }

    @Test
    fun coveredSlotHidesOnItsOwn() {
        val visibility = SlotVisibility(3)
        repeat(40) { visibility.onCheck(listOf(true, true, true)) }
        repeat(14) { visibility.onCheck(listOf(true, true, false)) }
        assertEquals(emptySet(), visibility.hidden)
        visibility.onCheck(listOf(true, true, false))
        assertEquals(setOf(2), visibility.hidden)
        repeat(200) { visibility.onCheck(listOf(true, true, false)) } // долго закрыт — так и остаётся скрытым
        assertEquals(setOf(2), visibility.hidden)
        visibility.onCheck(listOf(true, true, true))
        assertEquals(emptySet(), visibility.hidden)
    }

    @Test
    fun allHideOnlyAfterLongStretchWithNothingRecognized() {
        val visibility = SlotVisibility(3)
        repeat(100) { visibility.onCheck(listOf(true, false, false)) } // в бою узнаётся один слот — видны все
        assertEquals(emptySet(), visibility.hidden)
        repeat(14) { visibility.onCheck(listOf(false, false, false)) } // короткий провал узнавания не прячет
        assertEquals(emptySet(), visibility.hidden)
        visibility.onCheck(listOf(false, false, false))
        assertEquals(setOf(0, 1, 2), visibility.hidden)
        visibility.onCheck(listOf(true, false, false))
        assertEquals(emptySet(), visibility.hidden)
    }

    @Test
    fun nothingHidesWhenRecognitionRarelyWorks() {
        val visibility = SlotVisibility(3)
        repeat(300) { i -> visibility.onCheck(listOf(i % 50 == 0, false, false)) } // узнаётся раз в 5 с
        assertEquals(emptySet(), visibility.hidden)
    }

    @Test
    fun slotIsRecognizedOnlyWhenOwnIconIsClearlyCloser() {
        assertTrue(SlotVisibility.recognized(30.0, listOf(55.0, 60.0)))
        assertFalse(SlotVisibility.recognized(40.0, listOf(44.0, 60.0))) // фон: своя не лучше чужих
        assertFalse(SlotVisibility.recognized(null, listOf(44.0)))
    }
}
