package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertEquals

class SlotDarkeningTest {
    /** Цветность трёх слотов по кадрам, как в записи 2026-11-18: слот 0 уходит на перезарядку и плавно возвращается. */
    private fun run(frames: List<IntArray>): List<Pair<Int, Int>> {
        val darkening = SlotDarkening(3)
        return frames.flatMapIndexed { frame, values -> darkening.onFrame(values).map { frame to it } }
    }

    @Test
    fun cooldownStartIsSeenOnce() {
        val slot0 = listOf(53, 52, 53, 13, 13, 14, 16, 17, 19, 21, 26, 30, 33, 41, 45)
        val frames = slot0.map { intArrayOf(it, 69, 7) }
        assertEquals(listOf(3 to 0), run(frames))
    }

    @Test
    fun greySlotIsIgnored() {
        val frames = listOf(7, 8, 2, 7).map { intArrayOf(53, 69, it) }
        assertEquals(emptyList(), run(frames))
    }

    @Test
    fun severalSlotsDarkAtOnceAreNotAUse() {
        val frames = listOf(intArrayOf(53, 69, 61), intArrayOf(10, 12, 61))
        assertEquals(emptyList(), run(frames))
    }
}
