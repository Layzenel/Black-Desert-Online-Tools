package com.layzen.bdotools.feature.comboassist.domain.tracker

import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.ADAMANTINE
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.ANKLE_HOOK
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.BOMBARDMENT
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.CRIMSON_FANG
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.DEATHSTRIKE
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.ROARING_TIGER
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.SUPPRESSION
import com.layzen.bdotools.feature.comboassist.domain.StrikerFixture.CROUCHING_WOLF
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComboTrackerTest {
    private val step = 800L

    private fun tracker(combo: Combo = StrikerFixture.demo) = ComboTracker(combo, StrikerFixture.skills)

    /** Игрок жмёт подсказанное [steps] раз; возвращает подписи шагов по проходам и время после последнего шага. */
    private fun ComboTracker.follow(steps: Int, from: Long = 0): Pair<Map<Int, List<String>>, Long> {
        val passes = mutableMapOf<Int, MutableList<String>>()
        var t = from
        repeat(steps) {
            val next = planner.predict(state, t).first()
            t = maxOf(t, next.atMs)
            onSkill(next.skillNo, t)
            passes.getOrPut(state.pass) { mutableListOf() } += next.node!!.label
            t += step
        }
        return passes to t
    }

    private fun ComboTracker.label() = graph[state.positions.single()].label

    @Test
    fun forkLanesGoInTurn() {
        val (passes, _) = tracker().follow(40)
        assertEquals("1.1", passes.getValue(1).first())
        assertEquals("1.2", passes.getValue(2).first())
        assertEquals("1.3", passes.getValue(3).first()) // 1.1 уже откатилась, но 1.3 готова и дольше не проходилась
    }

    @Test
    fun poolSkillsAreNeverSuggested() {
        val tracker = tracker()
        val suggested = (0 until 40).flatMap { tracker.planner.predict(tracker.state, it * step).map { s -> s.skillNo } }
        assertTrue(suggested.none { it == SUPPRESSION || it == CROUCHING_WOLF })
    }

    @Test
    fun poolSkillKeepsPosition() {
        val tracker = tracker()
        val (_, t) = tracker.follow(2) // не внутри связки: умение пула посреди связки её обрывает
        val label = tracker.label()
        assertIs<TrackerEvent.Filler>(tracker.onSkill(SUPPRESSION, t))
        assertEquals(label, tracker.label())
    }

    @Test
    fun chainBreakBurnsTheRestAndPicksCheapestRecovery() {
        val tracker = tracker()
        val (_, t) = tracker.follow(3)
        assertEquals("3", tracker.label())
        val event = tracker.onSkill(ANKLE_HOOK, t)
        assertIs<TrackerEvent.Filler>(event)
        assertIs<ChainRecovery.Skip>(event.chain) // Стальное плечо на перезарядке, повтор дороже пропуска
        assertEquals(2, tracker.state.burned.size)
    }

    @Test
    fun chainRescuedThroughAnotherLauncher() {
        val combo = Combo(
            id = "c", name = "c", classKey = "striker", fillers = listOf(ANKLE_HOOK, ADAMANTINE),
            columns = listOf(
                ComboColumn.Single(ComboCell(CRIMSON_FANG)),
                ComboColumn.Single(ComboCell(DEATHSTRIKE, chain = true)),
                ComboColumn.Single(ComboCell(BOMBARDMENT, chain = true)),
            ),
        )
        val tracker = tracker(combo)
        tracker.onSkill(CRIMSON_FANG, 0)
        val event = tracker.onSkill(ANKLE_HOOK, step)
        assertEquals(ChainRecovery.Rescue(tracker.graph[1], ADAMANTINE), event.chain)
        val entry = tracker.planner.predict(tracker.state, 2 * step).first()
        assertTrue(entry.isEntry && entry.skillNo == ADAMANTINE)
        tracker.onSkill(ADAMANTINE, 2 * step)
        assertIs<TrackerEvent.Step>(tracker.onSkill(DEATHSTRIKE, 3 * step))
        assertEquals("2", tracker.label())
        assertEquals(1, tracker.state.pass)
        assertTrue(tracker.state.burned.isEmpty())
    }

    @Test
    fun chainIsNotRescuedThroughSkillOutsideCombo() {
        // 2026-11-20: трекер предлагал войти в связку умением, которого в комбо нет, — без иконки и непонятно откуда
        val combo = Combo(
            id = "c", name = "c", classKey = "striker", fillers = listOf(ANKLE_HOOK),
            columns = listOf(
                ComboColumn.Single(ComboCell(CRIMSON_FANG)),
                ComboColumn.Single(ComboCell(DEATHSTRIKE, chain = true)),
                ComboColumn.Single(ComboCell(BOMBARDMENT, chain = true)),
            ),
        )
        val tracker = tracker(combo)
        tracker.onSkill(CRIMSON_FANG, 0)
        val event = tracker.onSkill(ANKLE_HOOK, step)
        assertFalse(event.chain is ChainRecovery.Rescue)
    }

    @Test
    fun chainTimesOutWithoutFollowUp() {
        val tracker = tracker()
        val (_, t) = tracker.follow(3)
        assertEquals(null, tracker.tick(t))
        assertIs<TrackerEvent.ChainTimeout>(tracker.tick(t + 2000))
    }

    @Test
    fun outOfOrderSkillMovesPositionToIt() {
        val tracker = tracker()
        val (_, t) = tracker.follow(1)
        val event = tracker.onSkill(ROARING_TIGER, t)
        assertIs<TrackerEvent.ErrorForward>(event)
        assertEquals("6.1", tracker.label()) // позиция — на нажатом умении, подсказка — со следующего за ним
        assertEquals(StrikerFixture.KNEE_HAMMER, tracker.planner.predict(tracker.state, t + step).first().skillNo)
    }

    @Test
    fun skillPressedInsteadOfFirstStartsComboFromIt() {
        val tracker = tracker()
        tracker.onSkill(CRIMSON_FANG, 0) // вместо первого шага — третий
        assertEquals("3", tracker.label())
        assertEquals(DEATHSTRIKE, tracker.planner.predict(tracker.state, step).first().skillNo)
    }

    @Test
    fun idleResetsToStart() {
        val tracker = tracker()
        val (_, t) = tracker.follow(2)
        assertIs<TrackerEvent.Reset>(tracker.tick(t + 9000))
        assertTrue(tracker.state.atStart)
    }

    @Test
    fun fillerOnlyForLongWaitAndNotInsideChain() {
        val tracker = tracker()
        assertEquals(ANKLE_HOOK, tracker.planner.filler(tracker.state, listOf(ANKLE_HOOK), 0, 2000))
        assertEquals(null, tracker.planner.filler(tracker.state, listOf(ANKLE_HOOK), 0, 300))
        tracker.follow(3)
        assertNotNull(tracker.planner.expectedChain(tracker.state))
        assertEquals(null, tracker.planner.filler(tracker.state, listOf(ANKLE_HOOK), 0, 2000))
    }

    @Test
    fun staysOnLaneWhenSameSkillStartsBothLanes() {
        val (b, c, e) = Triple(1, 2, 3)
        val combo = Combo("l", "l", "striker", listOf(ComboColumn.Fork(listOf(listOf(ComboCell(b), ComboCell(c)), listOf(ComboCell(b), ComboCell(e))))))
        val tracker = ComboTracker(combo, com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills.Empty)
        tracker.onSkill(b, 0)
        tracker.onSkill(e, step) // вторая строка
        tracker.onSkill(b, 2 * step) // новый проход: B есть в обеих строках
        assertEquals("1.2", tracker.label())
        assertEquals(e, tracker.planner.predict(tracker.state, 3 * step).first().skillNo)
    }

    /** Как у игрока: развилка из двух длинных веток, в обеих одна и та же связка. */
    private val twoLanes = Combo(
        "t", "t", "striker", fillers = emptyList(), pool = listOf(StrikerFixture.CROUCHING_WOLF),
        columns = listOf(
            ComboColumn.Fork(
                listOf(
                    listOf(ComboCell(StrikerFixture.KNEE_HAMMER), ComboCell(CRIMSON_FANG), ComboCell(DEATHSTRIKE, chain = true), ComboCell(BOMBARDMENT, chain = true)),
                    listOf(ComboCell(CRIMSON_FANG), ComboCell(DEATHSTRIKE, chain = true), ComboCell(BOMBARDMENT, chain = true), ComboCell(ROARING_TIGER)),
                ),
            ),
        ),
    )

    @Test
    fun chainBreaksEvenWhenPositionIsAmbiguous() {
        val tracker = tracker(twoLanes)
        tracker.onSkill(CRIMSON_FANG, 0) // с начала: Алый коготь есть в обеих ветках
        val event = tracker.onSkill(StrikerFixture.ANKLE_HOOK, step)
        assertNotNull(event.chain)
        assertTrue(tracker.state.burned.size >= 2)
    }

    @Test
    fun relaunchIsNotANewPass() {
        val tracker = tracker()
        val (_, t) = tracker.follow(3)
        tracker.onSkill(ANKLE_HOOK, t)
        assertFalse(tracker.state.pass > 1)
    }
}
