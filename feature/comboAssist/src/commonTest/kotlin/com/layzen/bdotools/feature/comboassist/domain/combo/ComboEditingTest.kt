package com.layzen.bdotools.feature.comboassist.domain.combo

import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.ADAMANTINE
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.BOMBARDMENT
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.CRIMSON_FANG
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.DEATHSTRIKE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ComboEditingTest {
    private val skills = StrikerFixture.skills
    private val empty = Combo("c", "c", "striker", listOf(ComboColumn.Single(ComboCell())))

    @Test
    fun fillingLastCellAddsNewSlotAndSetsChainFromData() {
        val first = empty.fill(CellAt(0), CRIMSON_FANG, skills)
        assertEquals(CellAt(1), first.selection)
        val second = first.combo.fill(first.selection!!, DEATHSTRIKE, skills)
        assertEquals(3, second.combo.columns.size)
        assertTrue(second.combo[CellAt(1)]!!.chain)
    }

    @Test
    fun forkLanesGrowAndLabelLikeEditor() {
        val forked = empty.fill(CellAt(0), ADAMANTINE, skills).combo.toFork(0)
        assertEquals(CellAt(0, 1), forked.selection)
        val grown = forked.combo.growLane(0, 0)
        assertEquals(CellAt(0, 0, 1), grown.selection)
        assertEquals("2.1", grown.combo.label(CellAt(0, 0, 1)))
        assertEquals("3", grown.combo.label(CellAt(1)))
    }

    @Test
    fun removingLastButOneLaneUnwrapsFork() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Fork(listOf(listOf(ComboCell(1), ComboCell(2)), listOf(ComboCell(3))))))
        val result = combo.removeCell(CellAt(0, 1)).combo
        assertEquals(listOf(ComboColumn.Single(ComboCell(1)), ComboColumn.Single(ComboCell(2))), result.columns)
    }

    private fun row(vararg skills: Int) = Combo("c", "c", "striker", skills.map { ComboColumn.Single(ComboCell(it)) })

    private fun Combo.skillRows() = columns.map { column -> column.lanes.map { lane -> lane.map { it.skillNo } } }

    @Test
    fun moveAfterPutsCellBehindTargetWithItsFlags() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Single(ComboCell(1, chain = true)), ComboColumn.Single(ComboCell(2)), ComboColumn.Single(ComboCell(3))))
        val moved = combo.moveCell(CellAt(0), CellAt(1), DropSide.After).combo
        assertEquals(listOf(2, 1, 3), moved.columns.map { (it as ComboColumn.Single).cell.skillNo })
        assertEquals(ComboCell(1, chain = true), moved[CellAt(1)])
    }

    @Test
    fun moveBeforeGoesLeftOfTarget() {
        assertEquals(listOf(listOf(listOf(3)), listOf(listOf(1)), listOf(listOf(2))), row(1, 2, 3).moveCell(CellAt(2), CellAt(0), DropSide.Before).combo.skillRows())
    }

    @Test
    fun moveBelowSingleMakesFork() {
        val moved = row(1, 2, 3).moveCell(CellAt(2), CellAt(1), DropSide.Below).combo
        assertEquals(listOf(listOf(listOf(1)), listOf(listOf(2), listOf(3))), moved.skillRows())
    }

    @Test
    fun moveAboveAddsLaneToForkAndRemovesEmptySource() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Fork(listOf(listOf(ComboCell(1)), listOf(ComboCell(2)))), ComboColumn.Single(ComboCell(3))))
        val moved = combo.moveCell(CellAt(1), CellAt(0, 0), DropSide.Above).combo
        assertEquals(listOf(listOf(listOf(3), listOf(1), listOf(2))), moved.skillRows())
    }

    @Test
    fun forkCellDroppedBesideItselfLeavesFork() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Fork(listOf(listOf(ComboCell(1)), listOf(ComboCell(2)))), ComboColumn.Single(ComboCell(3))))
        val moved = combo.moveCell(CellAt(0, 1), CellAt(0, 1), DropSide.After).combo
        assertEquals(listOf(listOf(listOf(1)), listOf(listOf(2)), listOf(listOf(3))), moved.skillRows())
    }

    @Test
    fun forkCellDroppedBesideOtherLaneClosesFork() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Fork(listOf(listOf(ComboCell(1)), listOf(ComboCell(2))))))
        val moved = combo.moveCell(CellAt(0, 0), CellAt(0, 1), DropSide.Before).combo
        assertEquals(listOf(listOf(listOf(1)), listOf(listOf(2))), moved.skillRows())
    }

    @Test
    fun singleCellOnItselfDoesNothing() {
        assertEquals(row(1, 2).skillRows(), row(1, 2).moveCell(CellAt(0), CellAt(0), DropSide.After).combo.skillRows())
    }

    @Test
    fun moveInsideLaneReorders() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Fork(listOf(listOf(ComboCell(1), ComboCell(2)), listOf(ComboCell(3))))))
        val moved = combo.moveCell(CellAt(0, 0, 0), CellAt(0, 0, 1), DropSide.After).combo
        assertEquals(listOf(listOf(listOf(2, 1), listOf(3))), moved.skillRows())
    }

    @Test
    fun movingLastButOneLaneOutUnwrapsFork() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Fork(listOf(listOf(ComboCell(1)), listOf(ComboCell(2))))))
        val moved = combo.moveCell(CellAt(0, 1), CellAt(0, 0), DropSide.Before).combo
        assertEquals(listOf(listOf(listOf(2)), listOf(listOf(1))), moved.skillRows())
    }

    @Test
    fun followUpWithoutLauncherIsReported() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Single(ComboCell(ADAMANTINE)), ComboColumn.Single(ComboCell(BOMBARDMENT))))
        assertEquals(listOf(CellAt(1)), combo.chainProblems(skills))
        assertIs<ComboColumn.Single>(combo.insertColumn(1).combo.columns[1])
    }
}
