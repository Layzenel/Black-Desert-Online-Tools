package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SnapTest {
    @Test
    fun leftEdgeSnapsToOtherLeftEdge() {
        val snap = Snap.of(ImageRect(103f, 50f, 143f, 70f), listOf(ImageRect(100f, 0f, 120f, 20f)), reach = 5f)
        assertEquals(-3f, snap.dx)
        assertEquals(100f, snap.x)
    }

    @Test
    fun centerSnapsToImageCenter() {
        val snap = Snap.of(ImageRect(40f, 92f, 60f, 112f), listOf(ImageRect(0f, 0f, 200f, 200f)), reach = 5f)
        assertEquals(-2f, snap.dy)
        assertEquals(100f, snap.y)
    }

    @Test
    fun farAwayDoesNotSnap() {
        val snap = Snap.of(ImageRect(30f, 30f, 40f, 40f), listOf(ImageRect(0f, 0f, 10f, 10f)), reach = 5f)
        assertEquals(Snap(), snap)
        assertNull(snap.x)
    }

    @Test
    fun curvedArrowBendsSideways() {
        val arrow = Edit.Arrow(ImagePoint(0f, 0f), ImagePoint(100f, 0f), 0, 2f, ArrowStyle.Curved)
        assertEquals(ImagePoint(50f, 25f), arrow.bend)
        assertEquals(ImagePoint(100f, 0f), arrow.line.last())
    }

    @Test
    fun rhombusTouchesRectMidpoints() {
        val rhombus = Edit.Frame(ImageRect(0f, 0f, 20f, 10f), 0, 1f, FrameShape.Rhombus)
        assertEquals(listOf(ImagePoint(10f, 0f), ImagePoint(20f, 5f), ImagePoint(10f, 10f), ImagePoint(0f, 5f)), rhombus.outline)
    }
}
