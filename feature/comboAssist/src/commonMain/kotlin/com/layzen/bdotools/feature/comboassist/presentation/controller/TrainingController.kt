package com.layzen.bdotools.feature.comboassist.presentation.controller

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.cells
import com.layzen.bdotools.feature.comboassist.domain.graph.ComboNode
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyCommand
import com.layzen.bdotools.feature.comboassist.domain.keys.KeyStep
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills
import com.layzen.bdotools.feature.comboassist.domain.tracker.ComboTracker
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerSettings
import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.OverlayState
import kotlin.time.TimeSource

/**
 * Трекер комбо со временем: тренировка кликами и события истории умений из игры идут сюда одинаково.
 * Снимок для оверлея — [snapshot].
 */
internal class TrainingController {
    private val start = TimeSource.Monotonic.markNow()
    private var tracker: ComboTracker? = null
    private var combo: Combo? = null
    private var skills = ComboSkills.Empty
    private var lastEvent: TrackerEvent? = null
    private var lastEventMs = 0L

    /** Текущее время трекера, мс. */
    fun nowMs(): Long = start.elapsedNow().inWholeMilliseconds

    /** Начинает следить за комбо [combo] с умениями [skills] и порогами [settings]; перезарядки переносятся. */
    fun load(combo: Combo, skills: ComboSkills, settings: TrackerSettings) {
        this.combo = combo
        this.skills = skills
        // перезарядки переносятся: правка комбо не откатывает умения в игре
        val cooldowns = tracker?.state?.readyAt?.toMap().orEmpty()
        tracker = ComboTracker(combo, skills, settings).also { it.state.readyAt.putAll(cooldowns) }
        lastEvent = null
    }

    /** Игрок применил [skillNo] в предпросмотре: засчитывается всегда, даже если по расчёту оно ещё на перезарядке. */
    fun press(skillNo: Int) { tracker?.let { record(it.onSkill(skillNo, nowMs())) } }

    /** Умение [skillNo] появилось в истории умений игры: оно точно вышло, перезарядку не проверяем. */
    fun fromGame(skillNo: Int) { tracker?.let { record(it.onSkill(skillNo, nowMs())) } }

    /**
     * Шаги, которые можно подтвердить клавишами: те, которых трекер ждёт сейчас, и следующие за ними (строка
     * истории текущего ещё может не прийти, а продолжение уже жмут). [commands] — варианты клавиш умений.
     */
    fun keySteps(commands: Map<Int, KeyCommand>): List<KeyStep> {
        val tracker = tracker ?: return emptyList()
        val now = tracker.planner.candidates(tracker.state)
        val keyTracked = combo?.keyTracked.orEmpty()
        // умения из истории и со слотов клавишами не подтверждаются: те источники точнее
        fun step(node: ComboNode, after: Int?): KeyStep? {
            val skillNo = node.skillNo
            if (skillNo !in keyTracked) return null
            val command = commands[skillNo]
            // выбранная в редакторе команда — единственный вариант: остальные варианты игрок не жмёт
            val variants = combo?.get(node.at)?.keys?.let(::listOf) ?: command?.variants ?: return null
            return KeyStep(skillNo, after, variants, command?.visibleSpend ?: false)
        }
        val steps = now.mapNotNull { step(it, null) } + now.flatMap { node -> node.next.mapNotNull { step(it, node.skillNo) } }
        return steps.distinct()
    }

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
            pool = combo?.pool.orEmpty(),
            cellKeys = combo?.cells().orEmpty().mapNotNull { (at, cell) -> cell.keys?.let { at to it } }.toMap(),
            previousSkill = state.history.lastOrNull(),
            event = lastEvent?.takeIf { now - lastEventMs < EVENT_FRESH_MS },
            nowMs = now,
        )
    }

    private fun record(event: TrackerEvent) {
        lastEvent = event
        lastEventMs = nowMs()
    }

    private companion object {
        const val EVENT_FRESH_MS = 1800L
    }
}
