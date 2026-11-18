package com.layzen.bdotools.feature.comboassist.domain.combo

import kotlin.test.Test
import kotlin.test.assertEquals

class InsertCellTest {
    private val combo = Combo(
        "i", "i", "striker",
        columns = listOf(
            ComboColumn.Single(ComboCell(1)),
            ComboColumn.Fork(listOf(listOf(ComboCell(2), ComboCell(3)), listOf(ComboCell(4)))),
        ),
    )

    @Test
    fun singleColumnGetsANewColumnOnTheChosenSide() {
        val left = combo.insertCell(CellAt(0), after = false)
        assertEquals(listOf(null, 1), left.combo.columns.take(2).map { it.lanes[0][0].skillNo })
        assertEquals(CellAt(0), left.selection)
        val right = combo.insertCell(CellAt(0), after = true)
        assertEquals(listOf(1, null), right.combo.columns.take(2).map { it.lanes[0][0].skillNo })
        assertEquals(CellAt(1), right.selection)
    }

    @Test
    fun forkCellGetsANewCellInItsLane() {
        val edit = combo.insertCell(CellAt(1, 0, 1), after = false)
        assertEquals(listOf(2, null, 3), edit.combo.columns[1].lanes[0].map { it.skillNo })
        assertEquals(listOf(4), edit.combo.columns[1].lanes[1].map { it.skillNo })
        assertEquals(CellAt(1, 0, 1), edit.selection)
    }
}
