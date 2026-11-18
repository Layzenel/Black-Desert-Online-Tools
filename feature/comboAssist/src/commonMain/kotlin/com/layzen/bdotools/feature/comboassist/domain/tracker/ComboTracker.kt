package com.layzen.bdotools.feature.comboassist.domain.tracker

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.graph.ComboGraph
import com.layzen.bdotools.feature.comboassist.domain.graph.ComboNode
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills

/**
 * Следит, где игрок в комбо, по применённым умениям (тем, что вышли и попали в историю умений игры).
 *
 * Шаг по комбо двигает позицию; умение из комбо не по порядку — сбой: трекер сравнивает «вернуться» и «идти дальше»
 * и берёт дешёвое, а если умения нет на пути — перескакивает по последним умениям истории. Оборванная связка
 * спасается входом через другое умение, повтором с запуска или пропуском. Филлеры позицию не трогают. Время
 * подаётся снаружи: трекер не держит таймеров, обрыв связки и простой проверяет [tick].
 *
 * @property combo комбо
 * @property skills перезарядки и связки умений
 * @property settings темп и пороги
 */
class ComboTracker(private val combo: Combo, private val skills: ComboSkills, private val settings: TrackerSettings = TrackerSettings()) {
    /** Граф комбо. */
    val graph: ComboGraph = ComboGraph.of(combo)

    /** Подсказка по этому комбо. */
    val planner: ComboPlanner = ComboPlanner(graph, skills, settings)

    /** Где игрок сейчас. */
    val state: TrackerState = TrackerState()

    /** Игрок применил умение [skillNo] в момент [nowMs]. */
    fun onSkill(skillNo: Int, nowMs: Long): TrackerEvent {
        val before = planner.predict(state, nowMs)
        state.readyAt[skillNo] = nowMs + skills.cooldown(skillNo)
        state.history = (state.history + skillNo).takeLast(HISTORY_SIZE)
        state.lastUseMs = nowMs
        state.rescue?.let { if (!it.armed && it.entry == skillNo) { state.rescue = it.copy(armed = true); return TrackerEvent.Step(skillNo, rescueEntry = true) } }
        planner.candidates(state).filter { it.skillNo == skillNo }.takeIf { it.isNotEmpty() }?.let { return advance(it, skillNo) }
        val chain = planner.expectedChain(state)?.let { breakChain(it, nowMs) }
        if (chain != null) {
            planner.candidates(state).filter { it.skillNo == skillNo }.takeIf { it.isNotEmpty() }?.let { return advance(it, skillNo, chain) }
        }
        return when {
            skillNo in graph.skills -> { state.rescue = null; state.forced = null; outOfOrder(skillNo, before, chain) }
            skillNo in combo.fillers -> TrackerEvent.Filler(skillNo, chain)
            ++state.offCount >= settings.offLimit -> { resetPosition(); TrackerEvent.Reset(ResetReason.OffCombo, skillNo, chain) }
            else -> TrackerEvent.Off(skillNo, state.offCount, chain)
        }
    }

    /** Проверка по времени: связка, продолжение которой не пришло за окно, обрывается; после простоя — сначала. */
    fun tick(nowMs: Long): TrackerEvent? {
        val last = state.lastUseMs ?: return null
        val chainNext = planner.expectedChain(state)
        if (chainNext != null && nowMs - last > settings.chainWindowMs) return TrackerEvent.ChainTimeout(breakChain(chainNext, nowMs))
        if (!state.atStart && nowMs - last > settings.idleMs) { resetPosition(); return TrackerEvent.Reset(ResetReason.Idle) }
        return null
    }

    /** Начать комбо сначала. Перезарядки остаются: умения в игре не откатываются от сброса подсказки. */
    fun reset(): TrackerEvent { resetPosition(); return TrackerEvent.Reset(ResetReason.Manual) }

    private fun advance(hit: List<ComboNode>, skillNo: Int, chain: ChainRecovery? = null): TrackerEvent {
        // повтор связки с запуска или вход через другое умение — не новый проход
        val prev = if (state.forced != null || state.rescue != null) null else planner.current(state)
        val wrapped = prev != null && hit.any { prev.wrapsTo(it) }
        if (wrapped) { state.pass++; state.done.clear(); state.burned.clear(); state.forkLanes.clear() }
        val chosen = sameLane(planner.followLinked(hit, state.forkLanes))
        state.positions = chosen.map { it.id }
        rememberLane(chosen)
        chosen.forEach { node -> state.done += node.id; chainFrom(node).forEach { state.burned -= it.id } }
        state.forced = null; state.rescue = null; state.offCount = 0
        return TrackerEvent.Step(skillNo, wrapped, chain = chain)
    }

    private fun resetPosition() {
        state.positions = emptyList(); state.done.clear(); state.burned.clear(); state.forkLanes.clear()
        state.forced = null; state.rescue = null; state.offCount = 0
    }

    /** Связка оборвалась перед [brokenAt]: вход через другое готовое умение, повтор с запуска или пропуск — что дешевле. */
    private fun breakChain(brokenAt: ComboNode, nowMs: Long): ChainRecovery {
        val lost = chainFrom(brokenAt)
        // игрок мог быть в нескольких местах: сгорают продолжения во всех, дальше — как будто он был у [brokenAt]
        planner.expectedChains(state).forEach { start -> chainFrom(start).forEach { state.burned += it.id } }
        lost.forEach { state.burned += it.id }
        state.positions = brokenAt.previous.filter { it.id in state.positions }.map { it.id }.ifEmpty { state.positions }.take(1)
        val step = settings.stepMs
        val entry = skills[brokenAt.skillNo].after.firstOrNull { !skills[it].onlyAfter && state.cooldownLeft(it, nowMs) == 0L }
        val launcher = launcherOf(brokenAt)
        val options = buildList {
            entry?.let { add(ChainRecovery.Rescue(brokenAt, it) to step + planner.chainWait(brokenAt, nowMs + step, state.readyAt)) }
            add(ChainRecovery.Relaunch(brokenAt, launcher) to planner.chainWait(launcher, nowMs, state.readyAt))
            add(ChainRecovery.Skip(brokenAt, lost.size) to (lost.size * step * settings.skipWeight).toLong())
        }
        val best = options.minBy { it.second }.first
        when (best) {
            is ChainRecovery.Rescue -> state.rescue = TrackerState.Rescue(best.entry, brokenAt.id)
            is ChainRecovery.Relaunch -> state.forced = best.launcher.id
            is ChainRecovery.Skip -> state.positions = listOf(lost.last().id)
        }
        return best
    }

    /**
     * Умение из комбо не по порядку: позиция переходит на него, подсказка — со следующего за ним. Если оно впереди на
     * пути — пропущенные до него шаги сгорают; иначе место ищется по последним умениям истории.
     */
    private fun outOfOrder(skillNo: Int, before: List<PlannedStep>, chain: ChainRecovery?): TrackerEvent {
        val j = before.indexOfFirst { it.node?.skillNo == skillNo }
        if (j > 0) {
            before.take(j).forEach { step -> step.node?.let { state.burned += it.id } }
            val target = before[j].node!!
            state.positions = listOf(target.id); state.done += target.id
            rememberLane(listOf(target))
            return TrackerEvent.ErrorForward(skillNo, j, chain)
        }
        for (k in minOf(HISTORY_SIZE, state.history.size) downTo 1) {
            val tail = state.history.takeLast(k)
            val found = sameLane(graph.nodes.filter { matchesTail(it, tail) })
            if (found.isNotEmpty()) {
                state.positions = found.map { it.id }
                rememberLane(found)
                state.done.clear(); state.done += state.positions; state.burned.clear()
                return TrackerEvent.Jump(skillNo, k, chain)
            }
        }
        return TrackerEvent.Off(skillNo, state.offCount, chain)
    }

    /** Из нескольких одинаковых шагов — те, что в ветке, по которой игрок шёл ([TrackerState.lane]), если такие есть. */
    private fun sameLane(nodes: List<ComboNode>): List<ComboNode> =
        if (nodes.size < 2) nodes else nodes.filter { it.isFork && it.at.lane == state.lane }.ifEmpty { nodes }

    private fun rememberLane(nodes: List<ComboNode>) {
        nodes.singleOrNull()?.takeIf { it.isFork }?.let {
            state.lane = it.at.lane
            state.forkLanes[it.at.column] = it.at.lane
        }
    }

    private fun matchesTail(node: ComboNode, tail: List<Int>): Boolean =
        node.skillNo == tail.last() && (tail.size == 1 || node.previous.any { matchesTail(it, tail.dropLast(1)) })

    private fun chainFrom(node: ComboNode): List<ComboNode> = generateSequence(node) { it.chainNext() }.toList()

    private fun launcherOf(node: ComboNode): ComboNode {
        var cur = node
        while (cur.chain) cur = cur.previous.firstOrNull { it.next.contains(cur) && !it.optional } ?: break
        return cur
    }

    private companion object {
        /** Строк в истории умений игры. */
        const val HISTORY_SIZE = 3
    }
}
