package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Сводит три источника применённых умений в один поток. Чему верить, по убыванию: строки истории умений, слоты
 * перезарядок, клавиши с расходом ресурса ([com.layzen.bdotools.feature.comboassist.data.keys.KeyStepWatch]).
 * Одно применение, замеченное несколькими источниками за [sameMs], засчитывается один раз.
 *
 * История точная, но приходит с запозданием (0,1–0,75 с) и пишется не для всех умений. Слот, ушедший на
 * перезарядку, ждёт [waitMs] строку истории с тем же умением: пришла — слот отбрасывается, не пришла — умение
 * выпускается само. Если раньше пришла строка другого умения, а слот потемнел заметно раньше неё, умение слота
 * выпускается первым: применено оно раньше, и связка не обгонит свой запуск. Строка истории, пришедшая за [sameMs]
 * после такого же умения из слота или клавиш, — то же применение и отбрасывается.
 *
 * Клавиши слушаются только для умений, которых нет в истории и на слотах. Подтверждённое нажатие выпускается сразу
 * ([onTick] того же кадра). Нажатие продолжения ([onKey] с `after`) ждёт выпуска умения `after`, чтобы связка
 * не обогнала запуск; строка истории другого умения его отменяет, не дождалось за [sameMs] — отбрасывается.
 *
 * shortcut: [historyLagMs] — догадка о том, насколько строка истории запаздывает за слотом; уточнить по `watch.log`
 * (там оба момента), когда появятся записи, где история и слот видят одно применение.
 */
internal class HistoryMerge(
    private val waitMs: Long = 600,
    private val sameMs: Long = 1500,
    private val repeatMs: Long = 1000,
    private val historyLagMs: Long = 150,
) {
    private val slots = mutableListOf<Pair<Int, Long>>()
    private val keys = mutableListOf<PendingKey>()

    /** Умения, выпущенные слотом или клавишами раньше строки истории, и когда. */
    private val early = mutableMapOf<Int, Long>()

    /** Когда умение выпущено последний раз, из любого источника. */
    private val released = mutableMapOf<Int, Long>()

    /** История узнала умение [skillNo] в момент [nowMs] → умения к выпуску по порядку. */
    fun onHistory(skillNo: Int, nowMs: Long): List<Int> {
        slots.removeAll { it.first == skillNo }
        val earlier = slots.filter { it.second <= nowMs - historyLagMs }
        slots.removeAll(earlier)
        val out = earlier.map { it.first }.onEach { releaseEarly(it, nowMs) }
        keys.removeAll { it.skillNo == skillNo || (it.after != skillNo && it.after !in out) }
        if (early[skillNo]?.let { nowMs - it < sameMs } == true) {
            early.remove(skillNo)
            return out
        }
        released[skillNo] = nowMs
        return out + skillNo
    }

    /** Слот умения [skillNo] ушёл на перезарядку в момент [nowMs]. */
    fun onSlot(skillNo: Int, nowMs: Long) {
        if (released[skillNo]?.let { nowMs - it < sameMs } == true) return
        if (slots.none { it.first == skillNo }) slots += skillNo to nowMs
    }

    /**
     * Нажаты клавиши умения [skillNo] в момент [nowMs]; [after] — умение, после которого его ждут (`null` — ждут
     * сейчас). `false`, если нажатие не учтено: умение уже ждёт подтверждения (в том числе слотом) или только что
     * засчитано — одно применение не засчитывается дважды (за [repeatMs] умение не применить снова).
     */
    fun onKey(skillNo: Int, after: Int?, nowMs: Long): Boolean {
        if (released[skillNo]?.let { nowMs - it < repeatMs } == true) return false
        if (keys.any { it.skillNo == skillNo } || slots.any { it.first == skillNo }) return false
        keys += PendingKey(skillNo, after, nowMs)
        return true
    }

    /** Проверка в момент [nowMs] (после [onSlot] и [onKey] кадра) → слоты, которых история так и не показала, и нажатия, чей запуск уже выпущен. */
    fun onTick(nowMs: Long): List<Int> {
        val dueSlots = slots.filter { nowMs - it.second >= waitMs }
        slots.removeAll(dueSlots)
        val fromSlots = dueSlots.map { it.first }.onEach { releaseEarly(it, nowMs) }
        keys.removeAll { key -> !key.ready(nowMs) && nowMs - key.atMs > sameMs }
        val dueKeys = keys.filter { it.ready(nowMs) }
        keys.removeAll(dueKeys)
        return fromSlots + dueKeys.map { it.skillNo }.onEach { releaseEarly(it, nowMs) }
    }

    private fun releaseEarly(skillNo: Int, nowMs: Long) {
        early[skillNo] = nowMs
        released[skillNo] = nowMs
        slots.removeAll { it.first == skillNo }
    }

    private fun PendingKey.ready(nowMs: Long): Boolean =
        after == null || released[after]?.let { it >= atMs - sameMs && it <= nowMs } == true

    /** Нажатие, ждущее подтверждения: умение, после какого умения его ждут, и когда нажали. */
    private data class PendingKey(val skillNo: Int, val after: Int?, val atMs: Long)
}
