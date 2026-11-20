package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoryMergeKeysTest {
    private val a = 1
    private val b = 2

    @Test
    fun keyCountsAtOnce() {
        val merge = HistoryMerge()
        merge.onKey(a, null, 0)
        assertEquals(listOf(a), merge.onKeyTick(0))
    }

    @Test
    fun laterHistoryOfSameSkillIsSameUse() {
        val merge = HistoryMerge()
        merge.onKey(a, null, 0)
        merge.onKeyTick(0)
        assertEquals(emptyList(), merge.onHistory(a, 750))
    }

    @Test
    fun historyOfSkillKeysMissedMovesTracker() {
        // дешёвое умение клавиши не подтвердили — строка истории всё равно его выпускает
        val merge = HistoryMerge()
        assertEquals(listOf(a), merge.onHistory(a, 0))
    }

    @Test
    fun sameSkillFromKeysTwiceWithinSecondIsOneUse() {
        // зажатая клавиша и само нажатие забрали два проседания для одного 7990 (2026-11-20)
        val merge = HistoryMerge()
        merge.onKey(a, null, 0)
        assertEquals(listOf(a), merge.onKeyTick(0))
        assertFalse(merge.onKey(a, null, 108))
        assertEquals(emptyList(), merge.onKeyTick(108))
    }

    @Test
    fun skillOnceSeenInHistoryIsStillConfirmedByKeys() {
        val merge = HistoryMerge()
        merge.onHistory(a, 0) // игра пишет одно и то же умение в историю не всегда
        assertTrue(merge.onKey(a, null, 2000))
        assertEquals(listOf(a), merge.onKeyTick(2000))
    }

    @Test
    fun continuationWaitsForItsLauncher() {
        val merge = HistoryMerge()
        merge.onKey(b, a, 0) // продолжение нажали, пока строка запуска a ещё не пришла
        assertEquals(emptyList(), merge.onKeyTick(0))
        assertEquals(listOf(a), merge.onHistory(a, 300))
        assertEquals(listOf(b), merge.onKeyTick(300))
    }

    @Test
    fun otherSkillInHistoryCancelsWaitingContinuation() {
        val merge = HistoryMerge()
        merge.onKey(b, a, 0)
        assertEquals(listOf(3), merge.onHistory(3, 250))
        merge.onHistory(a, 400)
        assertEquals(emptyList(), merge.onKeyTick(400))
    }

    @Test
    fun continuationWithoutLauncherIsDropped() {
        val merge = HistoryMerge()
        merge.onKey(b, a, 0)
        assertEquals(emptyList(), merge.onKeyTick(1600))
        merge.onHistory(a, 1700)
        assertEquals(emptyList(), merge.onKeyTick(1700))
    }
}
