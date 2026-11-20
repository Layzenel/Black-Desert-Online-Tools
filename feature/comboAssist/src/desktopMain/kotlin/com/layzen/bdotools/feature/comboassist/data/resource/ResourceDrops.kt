package com.layzen.bdotools.feature.comboassist.data.resource

/**
 * Замеры полоски ресурса ([onFill]) и проседания по ним: каждое проседание — одно начавшееся умение (умение тратит
 * ресурс сразу, как вышло). Проседание забирает нажатие ([claim]): одно проседание — один шаг, а момент шага —
 * момент проседания, а не нажатия (игроки жмут следующее умение заранее, ещё во время анимации предыдущего).
 *
 * @property minDrop на сколько пикселей должна укоротиться заливка: меньше — дрожание замера
 */
internal class ResourceDrops(private val minDrop: Int) {
    private val samples = ArrayDeque<Pair<Long, Int>>()
    private val drops = ArrayDeque<Long>()
    private val claimed = mutableSetOf<Long>()

    /** Замер [fill] (длина заливки, px) в момент [nowMs]. */
    fun onFill(nowMs: Long, fill: Int) {
        // проседание: заливка короче наибольшего замера за [WINDOW_MS], с одного кадра — у Страйкера то же умение тут же
        // восполняет ресурс ударами; дрожание отсекает порог [minDrop]. Ресурс, ушедший за пару кадров, — одно проседание
        val before = samples.filter { nowMs - it.first <= WINDOW_MS }.maxOfOrNull { it.second }
        val recent = drops.lastOrNull()?.let { nowMs - it <= WINDOW_MS } == true
        if (before != null && before - fill >= minDrop && !recent) drops.addLast(nowMs)
        samples.addLast(nowMs to fill)
        while (samples.isNotEmpty() && nowMs - samples.first().first > KEEP_MS) samples.removeFirst()
        while (drops.isNotEmpty() && nowMs - drops.first() > KEEP_MS) claimed -= drops.removeFirst()
    }

    /**
     * Первое ещё не забранное проседание после нажатия в момент [pressMs] — его момент; оно забирается. `null`, если
     * ресурс с тех пор не тратился. Нажатие замечают на кадре позже, поэтому подходит и проседание чуть раньше него.
     */
    fun claim(pressMs: Long): Long? {
        val drop = drops.firstOrNull { it >= pressMs - SLACK_MS && it !in claimed } ?: return null
        claimed += drop
        return drop
    }

    /** Замеры от [fromMs] до [toMs] «момент:длина» — для журнала, когда проседания не нашлось. */
    fun trace(fromMs: Long, toMs: Long): String = samples.filter { it.first in fromMs..toMs }.joinToString(" ") { "${it.first}:${it.second}" }

    /** Было ли проседание (забранное или нет) от [fromMs] до [toMs] включительно. */
    fun droppedBetween(fromMs: Long, toMs: Long): Boolean = drops.any { it in fromMs..toMs }

    /**
     * Свежее (не старше [FRESH_MS] к моменту [nowMs]) ещё не забранное проседание — его момент; оно забирается. Для
     * зажатой клавиши: умения, которые игра применяет подряд, пока её держат, нажатий не дают — только проседания.
     */
    fun claimFresh(nowMs: Long, notBefore: Long = Long.MIN_VALUE): Long? {
        val drop = drops.lastOrNull { nowMs - it <= FRESH_MS && it >= notBefore && it !in claimed } ?: return null
        claimed += drop
        return drop
    }

    private companion object {
        const val FRESH_MS = 200L
        const val KEEP_MS = 5000L
        const val WINDOW_MS = 150L
        const val SLACK_MS = 60L
    }
}
