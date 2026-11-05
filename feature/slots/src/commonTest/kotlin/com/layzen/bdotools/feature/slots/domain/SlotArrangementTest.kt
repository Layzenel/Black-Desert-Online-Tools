package com.layzen.bdotools.feature.slots.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class SlotArrangementTest {

    private val a = SlotId(SlotGroup.Cooldown, 1)
    private val b = SlotId(SlotGroup.Cooldown, 2)
    private val c = SlotId(SlotGroup.Cooldown, 3)
    private val outsider = SlotId(SlotGroup.Cooldown, 4)
    private val layout = SlotLayout(
        mapOf(a to SlotPosition(-100, 10), b to SlotPosition(0, 40), c to SlotPosition(250, -20), outsider to SlotPosition(7, 7)),
    )
    private val all = setOf(a, b, c)

    @Test
    fun alignLeftPutsEdgesOnLeftmost() {
        val result = layout.aligned(all, AlignEdge.Left)
        assertEquals(listOf(-100, -100, -100), all.map { result[it]!!.x })
        assertEquals(SlotPosition(7, 7), result[outsider])
    }

    @Test
    fun alignBottomKeepsHorizontalPositions() {
        val result = layout.aligned(all, AlignEdge.Bottom)
        assertEquals(listOf(-100, 0, 250), all.map { result[it]!!.x })
        assertEquals(listOf(40, 40, 40), all.map { result[it]!!.y })
    }

    @Test
    fun alignCenterXMovesToGroupMiddle() {
        // Группа от -125 до 275: середина 75
        val result = layout.aligned(all, AlignEdge.CenterX)
        assertEquals(listOf(75, 75, 75), all.map { result[it]!!.x })
    }

    @Test
    fun distributeEvensGapsAndKeepsEnds() {
        // Рамки 50 шириной: от -125 до 275, внутри 400 − 150 = 250 пустоты, промежуток 125, центр среднего: -125 + 50 + 125 + 25 = 75
        val result = layout.distributed(all, horizontal = true)
        assertEquals(listOf(-100, 75, 250), all.map { result[it]!!.x })
        assertEquals(40, result[b]!!.y)
    }

    @Test
    fun distributeNeedsThreeElements() {
        assertEquals(layout, layout.distributed(setOf(a, b), horizontal = true))
    }

    @Test
    fun centerPutsGroupMiddleOnTarget() {
        val result = layout.centeredAt(setOf(a, c), SlotPosition(0, 0))
        // Середина группы была (75, -5): обе позиции сдвигаются на (-75, 5)
        assertEquals(SlotPosition(-175, 15), result[a])
        assertEquals(SlotPosition(175, -15), result[c])
    }

    @Test
    fun mirrorFlipsAroundAxis() {
        val result = layout.mirrored(setOf(a, c), axisX = 0)
        assertEquals(SlotPosition(100, 10), result[a])
        assertEquals(SlotPosition(-250, -20), result[c])
        assertEquals(layout[b], result[b])
    }
}
