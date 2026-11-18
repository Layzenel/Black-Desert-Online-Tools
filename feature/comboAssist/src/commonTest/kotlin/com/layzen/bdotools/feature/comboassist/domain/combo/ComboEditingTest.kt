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

    @Test
    fun swapMovesFlagsWithSkill() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Single(ComboCell(1, chain = true)), ComboColumn.Single(ComboCell(2))))
        val swapped = combo.swap(CellAt(0), CellAt(1)).combo
        assertEquals(ComboCell(2), swapped[CellAt(0)])
        assertEquals(ComboCell(1, chain = true), swapped[CellAt(1)])
    }

    @Test
    fun followUpWithoutLauncherIsReported() {
        val combo = Combo("c", "c", "striker", listOf(ComboColumn.Single(ComboCell(ADAMANTINE)), ComboColumn.Single(ComboCell(BOMBARDMENT))))
        assertEquals(listOf(CellAt(1)), combo.chainProblems(skills))
        assertIs<ComboColumn.Single>(combo.insertColumn(1).combo.columns[1])
    }
}
