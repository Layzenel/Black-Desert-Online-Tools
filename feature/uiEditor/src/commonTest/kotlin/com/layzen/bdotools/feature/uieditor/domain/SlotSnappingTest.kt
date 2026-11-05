package com.layzen.bdotools.feature.uieditor.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class SlotSnappingTest {

    private val slot = SlotId(SlotGroup.Cooldown, 1)

    /** Рамка слота 50×50 с левым краем в [left] и верхом в [top]. */
    private fun bounds(left: Double, top: Double) = SlotBounds(left, top, left + 50, top + 50)

    @Test
    fun edgeWithinDistanceSnapsToLine() {
        val result = snapToLines(bounds(104.0, 0.0), xLines = listOf(100.0), yLines = emptyList(), distance = 10)
        assertEquals(-4.0, result.dx)
        assertEquals(100.0, result.guides.x)
    }

    @Test
    fun edgeBeyondDistanceStaysFree() {
        val result = snapToLines(bounds(120.0, 0.0), xLines = listOf(100.0), yLines = emptyList(), distance = 10)
        assertEquals(SnapResult(0.0, 0.0, SnapGuides.None), result)
    }

    @Test
    fun centerCanSnapToo() {
        // Середина рамки: 40 + 25 = 65, линия 60
        val result = snapToLines(bounds(40.0, 0.0), xLines = listOf(60.0), yLines = emptyList(), distance = 6)
        assertEquals(-5.0, result.dx)
    }

    @Test
    fun nearestLineWins() {
        val result = snapToLines(bounds(100.0, 0.0), xLines = listOf(95.0, 102.0), yLines = emptyList(), distance = 10)
        assertEquals(102.0, result.guides.x)
    }

    @Test
    fun axesAreIndependent() {
        val result = snapToLines(bounds(103.0, 500.0), xLines = listOf(100.0), yLines = listOf(0.0), distance = 5)
        assertEquals(SnapResult(-3.0, 0.0, SnapGuides(x = 100.0, y = null)), result)
    }

    @Test
    fun boundsOfSlotAreCenteredOnPosition() {
        assertEquals(SlotBounds(-35.0, 15.0, 15.0, 65.0), SlotBounds.of(slot, SlotPosition(-10, 40)))
    }
}
