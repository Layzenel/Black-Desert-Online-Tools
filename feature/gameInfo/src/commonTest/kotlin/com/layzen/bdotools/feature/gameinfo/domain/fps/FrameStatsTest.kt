package com.layzen.bdotools.feature.gameinfo.domain.fps

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FrameStatsTest {
    /** Моменты кадров с шагом [stepMs], начиная с нуля. */
    private fun frames(count: Int, stepMs: Double): List<Double> = List(count) { it * stepMs }

    @Test
    fun evenFramesHaveNoFreezes() {
        val stats = FrameStats.of(frames(301, 10.0))!!
        assertEquals(100, stats.fps)
        assertEquals(100, stats.lowFps)
        assertEquals(0, stats.freezes)
        assertEquals(10.0, stats.worstMs)
        assertEquals(300, stats.frameTimesMs.size)
    }

    @Test
    fun longFrameIsFreezeAndDragsLowFps() {
        // 200 кадров по 5 мс и один на 50 мс: худший 1 % — 3 кадра (50, 5, 5 мс), в среднем 20 мс
        val even = frames(201, 5.0)
        val withFreeze = even + (even.last() + 50.0)
        val stats = FrameStats.of(withFreeze)!!
        assertEquals(1, stats.freezes)
        assertEquals(50.0, stats.worstMs)
        assertEquals(50, stats.lowFps)
    }

    @Test
    fun fpsCountsOnlyLastSecond() {
        val stats = FrameStats.of(frames(501, 10.0))!!
        assertEquals(100, stats.fps)
    }

    @Test
    fun tooFewFramesIsNull() {
        assertNull(FrameStats.of(listOf(1.0)))
    }
}
