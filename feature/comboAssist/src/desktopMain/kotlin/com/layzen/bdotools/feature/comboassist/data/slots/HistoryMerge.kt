package com.layzen.bdotools.feature.comboassist.data.slots

/**
 * Сводит два источника применённых умений в один поток. Главный — строки истории умений; слоты перезарядок
 * дописывают умение, только если история его пропустила (игра иногда не пишет строку).
 *
 * Слот, ушедший на перезарядку, ждёт [waitMs] строку истории с тем же умением. Пришла — слот отбрасывается.
 * Не пришла — умение выпускается само. Если раньше пришла строка другого умения, а слот потемнел заметно раньше
 * неё, умение слота выпускается первым: применено оно раньше, и связка не обгонит свой запуск.
 * Строка истории, пришедшая за [sameMs] после такого же умения из слота, — то же применение, она отбрасывается.
 *
 * shortcut: [historyLagMs] — догадка о том, насколько строка истории запаздывает за слотом; уточнить по `watch.log`
 * (там оба момента), когда появятся записи, где история и слот видят одно применение.
 */
internal class HistoryMerge(
    private val waitMs: Long = 600,
    private val sameMs: Long = 1500,
    private val historyLagMs: Long = 150,
) {
    private val pending = mutableListOf<Pair<Int, Long>>()
    private val fromHistory = mutableMapOf<Int, Long>()
    private val fromSlots = mutableMapOf<Int, Long>()

    /** История узнала умение [skillNo] в момент [nowMs] → умения к выпуску по порядку. */
    fun onHistory(skillNo: Int, nowMs: Long): List<Int> {
        pending.removeAll { it.first == skillNo }
        val earlier = pending.filter { it.second <= nowMs - historyLagMs }
        pending.removeAll(earlier)
        val out = release(earlier, nowMs)
        if (fromSlots[skillNo]?.let { nowMs - it < sameMs } == true) {
            fromSlots.remove(skillNo)
            return out
        }
        fromHistory[skillNo] = nowMs
        return out + skillNo
    }

    /** Слот умения [skillNo] ушёл на перезарядку в момент [nowMs]. */
    fun onSlot(skillNo: Int, nowMs: Long) {
        if (fromHistory[skillNo]?.let { nowMs - it < sameMs } == true) return
        pending += skillNo to nowMs
    }

    /** Проверка по времени в момент [nowMs] → умения слотов, которых история так и не показала. */
    fun onTick(nowMs: Long): List<Int> {
        val due = pending.filter { nowMs - it.second >= waitMs }
        pending.removeAll(due)
        return release(due, nowMs)
    }

    private fun release(slots: List<Pair<Int, Long>>, nowMs: Long): List<Int> =
        slots.map { (skillNo, _) -> skillNo.also { fromSlots[it] = nowMs } }
}
