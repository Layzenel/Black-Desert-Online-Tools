package com.layzen.bdotools.feature.comboassist.presentation.controller

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import com.layzen.bdotools.feature.comboassist.domain.tracker.ComboTracker
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.LogLine
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import kotlin.time.TimeSource

/**
 * Трекер комбо со временем: тренировка кликами и события истории умений из игры идут сюда одинаково.
 * Снимок для оверлея — [snapshot], журнал — [log].
 */
internal class TrainingController {
    private val start = TimeSource.Monotonic.markNow()
    private var tracker: ComboTracker? = null
    private var combo: Combo? = null
    private var skills = ComboSkills.Empty
    private var lastEvent: TrackerEvent? = null
    private var lastEventMs = 0L
    private var nextLogId = 0L

    /** Журнал, новые сверху. */
    var log: List<LogLine> = emptyList(); private set

    /** Текущее время трекера, мс. */
    fun nowMs(): Long = start.elapsedNow().inWholeMilliseconds

    /** Начинает следить за комбо [combo] с умениями [skills]; перезарядки сбрасываются. */
    fun load(combo: Combo, skills: ComboSkills) {
        this.combo = combo
        this.skills = skills
        // перезарядки переносятся: правка комбо не откатывает умения в игре
        val cooldowns = tracker?.state?.readyAt?.toMap().orEmpty()
        tracker = ComboTracker(combo, skills).also { it.state.readyAt.putAll(cooldowns) }
        lastEvent = null
        log = emptyList()
    }

    /** Игрок применил [skillNo]. На перезарядке оно в игре не выходит — в журнал, но не в трекер. */
    fun press(skillNo: Int) {
        val tracker = tracker ?: return
        val now = nowMs()
        if (tracker.state.cooldownLeft(skillNo, now) > 0) { addLog(LogLine(nextLogId++, null, null, onCooldown = skillNo)); return }
        record(tracker.onSkill(skillNo, now))
    }

    /** Умение [skillNo] появилось в истории умений игры: оно точно вышло, перезарядку не проверяем. */
    fun fromGame(skillNo: Int) { tracker?.let { record(it.onSkill(skillNo, nowMs())) } }

    /** Жмёт подсказанное умение. */
    fun pressSuggested() { tracker?.let { t -> t.planner.predict(t.state, nowMs()).firstOrNull()?.let { press(it.skillNo) } } }

    /** Жмёт случайное готовое умение комбо, кроме подсказанного: проверка, как оверлей переживает сбой. */
    fun pressMistake() {
        val tracker = tracker ?: return
        val now = nowMs()
        val wanted = tracker.planner.predict(tracker.state, now).firstOrNull()?.skillNo
        tracker.graph.skills.filter { it != wanted && tracker.state.cooldownLeft(it, now) == 0L }.randomOrNull()?.let(::press)
    }

    /** Начать комбо сначала. */
    fun reset() { tracker?.let { record(it.reset()) } }

    /** Проверка по времени (обрыв связки, простой); `true`, если что-то случилось. */
    fun tick(): Boolean = tracker?.tick(nowMs())?.also(::record) != null

    /** Снимок трекера для оверлея. */
    fun snapshot(): OverlayState {
        val tracker = tracker ?: return OverlayState()
        val now = nowMs()
        val state = tracker.state
        val plan = tracker.planner.predict(state, now)
        val wait = plan.firstOrNull()?.let { it.atMs - now } ?: 0
        return OverlayState(
            nodeAt = tracker.graph.nodes.associate { it.at to it.id },
            plan = plan,
            current = state.positions.toSet(),
            done = state.done.toSet(),
            burned = state.burned.toSet(),
            pass = if (state.atStart) 0 else state.pass,
            cooldownLeft = state.readyAt.mapValues { (it.value - now).coerceAtLeast(0) }.filterValues { it > 0 },
            filler = tracker.planner.filler(state, combo?.fillers.orEmpty(), now, wait),
            previousSkill = state.history.lastOrNull(),
            event = lastEvent?.takeIf { now - lastEventMs < EVENT_FRESH_MS },
            nowMs = now,
        )
    }

    private fun record(event: TrackerEvent) {
        lastEvent = event
        lastEventMs = nowMs()
        val tracker = tracker ?: return
        val where = tracker.state.positions.singleOrNull()?.let { tracker.graph[it].label }
        addLog(LogLine(nextLogId++, event, where))
    }

    private fun addLog(line: LogLine) { log = (listOf(line) + log).take(LOG_SIZE) }

    private companion object {
        const val EVENT_FRESH_MS = 1800L
        const val LOG_SIZE = 40
    }
}
