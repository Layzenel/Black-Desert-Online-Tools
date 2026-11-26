package com.layzen.bdotools.feature.comboassist.domain.tracker

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkill
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ForkRotationTest {
    private val step = 800L

    /** Как у игрока: 1.x → 2 → 3 → 4.x → 5.x. */
    private val combo = Combo(
        "r", "r", "agent",
        columns = listOf(
            ComboColumn.Fork(listOf(listOf(ComboCell(A1)), listOf(ComboCell(A2)), listOf(ComboCell(A3)))),
            ComboColumn.Single(ComboCell(B)),
            ComboColumn.Single(ComboCell(C)),
            ComboColumn.Fork(listOf(listOf(ComboCell(D1), ComboCell(E1)), listOf(ComboCell(D2), ComboCell(E2)), listOf(ComboCell(D3), ComboCell(E3)))),
        ),
    )

    private fun ComboTracker.press(vararg skills: Int) = skills.forEachIndexed { i, skill -> onSkill(skill, i * step) }

    private fun ComboTracker.next(atMs: Long) = planner.predict(state, atMs).map { it.skillNo }

    @Test
    fun forksGoInTurnWhenEverythingIsReady() {
        val tracker = ComboTracker(combo, ComboSkills.Empty)
        tracker.press(A1, B, C, D1, E1)
        assertEquals(listOf(A2, B, C, D2, E2), tracker.next(5 * step))
    }

    @Test
    fun thirdPassTakesTheUnusedLanes() {
        val tracker = ComboTracker(combo, ComboSkills.Empty)
        tracker.press(A1, B, C, D1, E1, A2, B, C, D2, E2)
        assertEquals(listOf(A3, B, C, D3, E3), tracker.next(10 * step))
    }

    @Test
    fun pressedOtherLaneContinuesFromIt() {
        val tracker = ComboTracker(combo, ComboSkills.Empty)
        tracker.press(A1, B, C, D1, E1, A2, B, C, D2, E2)
        assertEquals(A3, tracker.next(10 * step).first())
        tracker.onSkill(A1, 10 * step) // подсказка 1.3, а нажали 1.1
        assertEquals(listOf(B, C), tracker.next(11 * step).take(2))
        assertEquals("1.1", tracker.graph[tracker.state.positions.single()].label)
    }

    @Test
    fun idleResetCountsFromTheMomentTheNextStepIsReady() {
        val tracker = ComboTracker(twoLanes, skills(cooldownA1 = 0))
        tracker.press(A1)
        assertEquals(null, tracker.tick(5 * step)) // 4 с простоя — ещё рано
        assertIs<TrackerEvent.Reset>(tracker.tick(8 * step))
        assertTrue(tracker.state.atStart)
    }

    @Test
    fun idleResetStartsLanesFromTheFirst() {
        val tracker = ComboTracker(twoLanes, ComboSkills.Empty)
        tracker.press(A1, B)
        assertIs<TrackerEvent.Reset>(tracker.tick(9 * step))
        assertEquals(A1, tracker.next(9 * step).first())
    }

    @Test
    fun olderLaneWaitsUpToOneStep() {
        val tracker = ComboTracker(twoLanes, skills(cooldownA1 = 4 * step + step / 2))
        tracker.press(A1, B, A2, B)
        assertEquals(A1, tracker.next(4 * step).first()) // A1 откатится через полшага — очередь важнее
    }

    @Test
    fun readyLaneWinsWhenTheOlderOneIsFarOnCooldown() {
        val tracker = ComboTracker(twoLanes, skills(cooldownA1 = 10 * step))
        tracker.press(A1, B, A2, B)
        assertEquals(A2, tracker.next(4 * step).first())
    }

    private val twoLanes = Combo(
        "t", "t", "agent",
        columns = listOf(ComboColumn.Fork(listOf(listOf(ComboCell(A1)), listOf(ComboCell(A2)))), ComboColumn.Single(ComboCell(B))),
    )

    private fun skills(cooldownA1: Long) = ComboSkills(mapOf(A1 to ComboSkill(A1, cooldownA1)))

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
