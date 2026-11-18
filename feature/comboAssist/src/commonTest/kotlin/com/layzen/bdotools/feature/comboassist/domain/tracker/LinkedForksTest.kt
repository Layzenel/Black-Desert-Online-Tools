package com.layzen.bdotools.feature.comboassist.domain.tracker

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkedForksTest {
    private val step = 800L

    /** Как у игрока: 1.x → 2 → 3 → 4.x → 5.x, и ветка второй развилки та же, что у первой. */
    private fun combo(linked: Boolean) = Combo(
        "l", "l", "agent",
        columns = listOf(
            ComboColumn.Fork(listOf(listOf(ComboCell(A1)), listOf(ComboCell(A2)), listOf(ComboCell(A3)))),
            ComboColumn.Single(ComboCell(B)),
            ComboColumn.Single(ComboCell(C)),
            ComboColumn.Fork(
                listOf(listOf(ComboCell(D1), ComboCell(E1)), listOf(ComboCell(D2), ComboCell(E2)), listOf(ComboCell(D3), ComboCell(E3))),
                linked = linked,
            ),
        ),
    )

    private fun ComboTracker.press(vararg skills: Int) = skills.forEachIndexed { i, skill -> onSkill(skill, i * step) }

    private fun ComboTracker.next() = planner.predict(state, 10 * step).map { it.skillNo }

    @Test
    fun linkedForkContinuesTheLaneOfThePreviousFork() {
        val tracker = ComboTracker(combo(linked = true), ComboSkills.Empty)
        tracker.press(A2, B, C)
        assertEquals(listOf(D2, E2), tracker.next())
    }

    @Test
    fun unlinkedForkPicksItsOwnLane() {
        val tracker = ComboTracker(combo(linked = false), ComboSkills.Empty)
        tracker.press(A2, B, C)
        assertEquals(listOf(D1, E1), tracker.next()) // без связи — первая ветка: перезарядок нет, все готовы
    }

    @Test
    fun linkIsPlannedAheadFromTheStartOfThePass() {
        val tracker = ComboTracker(combo(linked = true), ComboSkills.Empty)
        tracker.press(A3)
        assertEquals(listOf(B, C, D3, E3), tracker.next())
    }

    @Test
    fun newPassForgetsTheOldLane() {
        val tracker = ComboTracker(combo(linked = true), ComboSkills.Empty)
        tracker.press(A3, B, C, D3, E3, A1, B, C)
        assertEquals(listOf(D1, E1), tracker.next())
    }

    private companion object {
        const val A1 = 1
        const val A2 = 2
        const val A3 = 3
        const val B = 4
        const val C = 5
        const val D1 = 6
        const val D2 = 7
        const val D3 = 8
        const val E1 = 9
        const val E2 = 10
        const val E3 = 11
    }
}
