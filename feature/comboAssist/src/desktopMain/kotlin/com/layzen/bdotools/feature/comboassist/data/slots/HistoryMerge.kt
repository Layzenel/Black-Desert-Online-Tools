package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Сводит два источника применённых умений в один поток. Строки истории умений — точные, но приходят с запозданием
 * (0,1–0,75 с) и пишутся не для всех умений: продолжения связок и отмены анимации игра пропускает. Клавиши — самые
 * быстрые: шаг, которого трекер ждёт, засчитывается, как только умение началось
 * ([com.layzen.bdotools.feature.comboassist.data.keys.KeyStepWatch]), а строка истории того же умения за [sameMs]
 * после него — то же применение и отбрасывается. Если клавиши ошиблись, строка другого умения поправит трекер.
 *
 * Подтверждённое нажатие выпускается сразу ([onKeyTick] того же кадра). Нажатие продолжения ([onKey] с `after`)
 * ждёт выпуска умения `after` (строка запуска ещё не пришла), чтобы связка не обогнала запуск; строка истории
 * другого умения его отменяет, не дождалось за [sameMs] — отбрасывается.
 */
internal class HistoryMerge(
    private val sameMs: Long = 1500,
    private val repeatMs: Long = 1000,
) {
    private val keys = mutableListOf<PendingKey>()
    private val fromKeys = mutableMapOf<Int, Long>()
    private val released = mutableMapOf<Int, Long>()

    /** История узнала умение [skillNo] в момент [nowMs] → умения к выпуску (пусто, если клавиши его уже выпустили). */
    fun onHistory(skillNo: Int, nowMs: Long): List<Int> {
        keys.removeAll { it.skillNo == skillNo || it.after != skillNo }
        if (fromKeys[skillNo]?.let { nowMs - it < sameMs } == true) {
            fromKeys.remove(skillNo)
            return emptyList()
        }
        released[skillNo] = nowMs
        return listOf(skillNo)
    }

    /**
     * Нажаты клавиши умения [skillNo] в момент [nowMs]; [after] — умение, после которого его ждут (`null` — ждут
     * сейчас). `false`, если нажатие не учтено: умение уже ждёт подтверждения или только что засчитано — одно
     * применение не засчитывается дважды (за [repeatMs] умение не применить снова).
     */
    fun onKey(skillNo: Int, after: Int?, nowMs: Long): Boolean {
        if (released[skillNo]?.let { nowMs - it < repeatMs } == true) return false
        if (keys.any { it.skillNo == skillNo }) return false
        keys += PendingKey(skillNo, after, nowMs)
        return true
    }

    /** Нажатия к выпуску в момент [nowMs] (после [onKey] кадра): все, чей запуск уже выпущен. */
    fun onKeyTick(nowMs: Long): List<Int> {
        keys.removeAll { key -> !key.ready(nowMs) && nowMs - key.atMs > sameMs }
        val due = keys.filter { it.ready(nowMs) }
        keys.removeAll(due)
        return due.map { it.skillNo }.onEach { fromKeys[it] = nowMs; released[it] = nowMs }
    }

    private fun PendingKey.ready(nowMs: Long): Boolean =
        after == null || released[after]?.let { it >= atMs - sameMs && it <= nowMs } == true

    /** Нажатие, ждущее подтверждения: умение, после какого умения его ждут, и когда нажали. */
    private data class PendingKey(val skillNo: Int, val after: Int?, val atMs: Long)
}
