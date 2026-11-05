package com.layzen.bdotools.feature.uieditor.domain

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
    fun alignToAnchorKeepsAnchorInPlace() {
        // Опора — b: a и c встают верхом на её верх, сама b не двигается
        val result = layout.aligned(all, AlignEdge.Top, anchor = b)
        assertEquals(listOf(40, 40, 40), all.map { result[it]!!.y })
        assertEquals(layout[b], result[b])
    }

    @Test
    fun anchorOutsideSelectionFallsBackToGroup() {
        val result = layout.aligned(all, AlignEdge.Left, anchor = outsider)
        assertEquals(listOf(-100, -100, -100), all.map { result[it]!!.x })
    }

    @Test
    fun distributePutsGivenGapAndKeepsFirst() {
        // Рамки 50 шириной, первая (a) на месте: левые края -125, -65, -5 — центры -100, -40, 20
        val result = layout.distributed(all, horizontal = true, gap = 10)
        assertEquals(listOf(-100, -40, 20), all.map { result[it]!!.x })
        assertEquals(40, result[b]!!.y)
    }

    @Test
    fun distributeMovesColumnsOfGridTogether() {
        // Два ряда по три слота, средняя колонка сдвинута вправо: колонки выравниваются, ряды не перемешиваются
        val top = (1..3).map { SlotId(SlotGroup.Cooldown, it) }
        val bottom = (4..6).map { SlotId(SlotGroup.Cooldown, it) }
        val xs = listOf(0, 80, 200)
        val grid = SlotLayout((top.zip(xs) + bottom.zip(xs)).associate { (id, x) -> id to SlotPosition(x, if (id in top) 0 else 50) })
        val result = grid.distributed((top + bottom).toSet(), horizontal = true, gap = 25)
        assertEquals(listOf(0, 75, 150), top.map { result[it]!!.x })
        assertEquals(listOf(0, 75, 150), bottom.map { result[it]!!.x })
        assertEquals(listOf(50, 50, 50), bottom.map { result[it]!!.y })
    }

    @Test
    fun distributeNeedsTwoColumns() {
        assertEquals(layout, layout.distributed(setOf(a), horizontal = true, gap = 0))
        // Два элемента вплотную: b встаёт сразу справа от a
        assertEquals(SlotPosition(-50, 40), layout.distributed(setOf(a, b), horizontal = true, gap = 0)[b])
    }
}
