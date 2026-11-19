package com.layzen.bdotools.feature.comboassist.domain.tracker

import com.layzen.bdotools.feature.comboassist.domain.graph.ComboGraph
import com.layzen.bdotools.feature.comboassist.domain.graph.ComboNode
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills

/**
 * Подсказка: что жать дальше и когда. На развилке выбирает ветку, которая откатится раньше всех к моменту, когда до
 * её умений дойдёт очередь; связку оценивает целиком, чтобы не начинать её, если продолжения не успеют откатиться.
 * Ветки, которые откатятся почти одновременно (разница не больше шага), идут по очереди: первой — та, что дольше
 * всех не проходилась.
 * Опциональные шаги не подсказывает.
 *
 * @property graph граф комбо
 * @property skills перезарядки и связки умений
 * @property settings темп и пороги
 */
class ComboPlanner(private val graph: ComboGraph, private val skills: ComboSkills, private val settings: TrackerSettings) {

    /** Шаги, которые могут быть следующими по комбо (без учёта перезарядки). */
    fun candidates(state: TrackerState): List<ComboNode> {
        state.rescue?.let { return if (it.armed) listOf(graph[it.target]) else emptyList() }
        state.forced?.let { return listOf(graph[it]) }
        val next = if (state.atStart) graph.start else state.positions.flatMap { graph[it].next }
        return next.distinct().filter { it.id !in state.burned }
    }

    /** Последний пройденный шаг, если позиция однозначна. */
    fun current(state: TrackerState): ComboNode? = state.positions.singleOrNull()?.let { graph[it] }

    /**
     * Связка, которую сейчас нельзя прервать: продолжение после текущего шага. Если игрок может быть в нескольких
     * местах (одно умение в двух ветках), связка ждётся, когда после каждого из них идёт продолжение одного и того же
     * умения — тогда возвращается первое из них.
     */
    fun expectedChain(state: TrackerState): ComboNode? {
        if (state.forced != null || state.rescue != null || state.atStart) return null
        val next = state.positions.map { pos -> graph[pos].next.firstOrNull { it.chain && it.id !in state.burned } }
        if (next.any { it == null } || next.map { it!!.skillNo }.distinct().size != 1) return null
        return next.first()
    }

    /** Продолжения связки после всех мест, где может быть игрок: при обрыве сгорают все. */
    fun expectedChains(state: TrackerState): List<ComboNode> =
        if (expectedChain(state) == null) emptyList() else state.positions.mapNotNull { pos -> graph[pos].next.firstOrNull { it.chain } }

    /** Путь до конца прохода: что жать и когда. Первым шагом бывает вход в оборванную связку. */
    fun predict(state: TrackerState, nowMs: Long): List<PlannedStep> {
        val path = mutableListOf<PlannedStep>()
        val ready = state.readyAt.toMutableMap()
        var t = nowMs
        var prev = if (state.forced != null || state.rescue != null) null else current(state)
        val used = state.usedAt.toMutableMap()
        var candidates = candidates(state)
        state.rescue?.let { rescue ->
            if (!rescue.armed) {
                t += ((ready[rescue.entry] ?: 0) - t).coerceAtLeast(0)
                path += PlannedStep(rescue.entry, t, null)
                ready[rescue.entry] = t + skills.cooldown(rescue.entry)
                t += settings.stepMs
            }
            candidates = listOf(graph[rescue.target])
        }
        while (candidates.isNotEmpty() && path.size < settings.predictLimit) {
            val (node, wait) = pick(candidates, t, ready, used)
            if (node.optional) break
            if (prev != null && prev.wrapsTo(node) && path.isNotEmpty()) break
            t += wait
            path += PlannedStep(node.skillNo, t, node)
            used[node.id] = t
            ready[node.skillNo] = t + skills.cooldown(node.skillNo)
            t += settings.stepMs
            prev = node
            candidates = node.next.filter { it.id !in state.burned }
        }
        return path
    }

    /** Ожидание, чтобы пройти от [node] до конца его ветки и связки при старте в [startMs]. */
    fun chainWait(node: ComboNode, startMs: Long, ready: Map<Int, Long>): Long {
        var wait = 0L
        var cur: ComboNode? = node
        var i = 0
        while (cur != null) {
            wait = maxOf(wait, (ready[cur.skillNo] ?: 0) - (startMs + i * settings.stepMs))
            cur = cur.chainNext() ?: cur.nextInLane()
            i++
        }
        return wait.coerceAtLeast(0)
    }

    /** Филлер на ожидание [waitMs]: закреплённый в комбо, готовый, не продолжение связки; `null` — ждать без него. */
    fun filler(state: TrackerState, fillers: List<Int>, nowMs: Long, waitMs: Long): Int? {
        if (waitMs < settings.fillerMinMs || expectedChain(state) != null) return null
        if (state.rescue?.armed == false || settings.animationMs > waitMs + FILLER_SLACK_MS) return null
        return fillers.firstOrNull { it !in graph.skills && !skills[it].onlyAfter && state.cooldownLeft(it, nowMs) == 0L }
    }

    /** Шаг с наименьшим ожиданием; из почти равных (не дольше шага сверх лучшего) — давно не пройденный. */
    private fun pick(candidates: List<ComboNode>, t: Long, ready: Map<Int, Long>, used: Map<Int, Long>): Pair<ComboNode, Long> {
        val live = candidates.filter { !it.optional }.ifEmpty { candidates }
        val waits = live.map { it to chainWait(it, t, ready) }
        val best = waits.minOf { it.second }
        return waits.filter { it.second <= best + settings.stepMs }
            .minWith(compareBy<Pair<ComboNode, Long>> { used[it.first.id] ?: Long.MIN_VALUE }.thenBy { it.first.id })
    }

    private companion object {
        /** Насколько анимация филлера может не влезть в ожидание. */
        const val FILLER_SLACK_MS = 100L
    }
}
