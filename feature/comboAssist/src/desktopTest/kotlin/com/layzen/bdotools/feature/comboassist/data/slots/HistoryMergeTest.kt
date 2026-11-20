package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryMergeTest {
    private val a = 1
    private val b = 2

    @Test
    fun slotSeenByHistoryIsNotRepeated() {
        val merge = HistoryMerge()
        merge.onSlot(a, 0)
        assertEquals(listOf(a), merge.onHistory(a, 100))
        assertEquals(emptyList(), merge.onTick(1000))
    }

    @Test
    fun historyFirstThenSlotIsOneUse() {
        val merge = HistoryMerge()
        assertEquals(listOf(a), merge.onHistory(a, 0))
        merge.onSlot(a, 200)
        assertEquals(emptyList(), merge.onTick(1000))
    }

    @Test
    fun skillMissingInHistoryComesFromSlot() {
        val merge = HistoryMerge()
        merge.onSlot(a, 0)
        assertEquals(emptyList(), merge.onTick(300))
        assertEquals(listOf(a), merge.onTick(600))
        assertEquals(emptyList(), merge.onHistory(a, 900)) // строка всё же пришла поздно — то же применение
    }

    @Test
    fun missedSkillGoesBeforeLaterHistoryLine() {
        val merge = HistoryMerge()
        merge.onSlot(a, 0)
        assertEquals(listOf(a, b), merge.onHistory(b, 300)) // связка b не обгоняет свой запуск a
    }

    @Test
    fun keysOfSkillSeenBySlotAreIgnored() {
        // слоту верим больше, чем клавишам: умение со слота не засчитывается второй раз по нажатию
        val merge = HistoryMerge()
        merge.onSlot(a, 0)
        assertEquals(false, merge.onKey(a, null, 50))
        assertEquals(listOf(a), merge.onTick(600))
        assertEquals(false, merge.onKey(a, null, 700))
    }

    @Test
    fun slotAfterKeysIsSameUse() {
        val merge = HistoryMerge()
        merge.onKey(a, null, 0)
        assertEquals(listOf(a), merge.onTick(0))
        merge.onSlot(a, 100)
        assertEquals(emptyList(), merge.onTick(1000))
    }

    @Test
    fun slotRightBeforeHistoryLineWaits() {
        val merge = HistoryMerge()
        merge.onSlot(a, 250)
        assertEquals(listOf(b), merge.onHistory(b, 300)) // a применён после b: его строка ещё придёт
        assertEquals(listOf(a), merge.onHistory(a, 400))
    }
}
