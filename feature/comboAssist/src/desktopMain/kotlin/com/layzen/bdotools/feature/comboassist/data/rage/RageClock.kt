package com.layzen.bdotools.feature.comboassist.data.rage

/**
 * Минутный таймер гнева Чёрного духа. Гнев длится 60 с; начало видно по трём бафам подряд с таймером «59 с.»
 * ([RageTimerReader]). Тогда до конца ещё [remainingMs], и всё это время гнев активен — что бы ни было на экране
 * дальше: те же иконки бывают у других бафов, а свои могут закрыть окна. «59 с.» висит около секунды, и повторные
 * кадры с ним таймер не продлевают: новое начало засчитывается, только когда до конца прошлого осталось меньше
 * [restartMs] (за минуту гнев не перезапустить).
 */
internal class RageClock(private val remainingMs: Long = 59_000, private val restartMs: Long = 50_000) {
    /** Гнев активен. */
    var active: Boolean = false
        private set

    private var untilMs = Long.MIN_VALUE
    private var checked = false

    /**
     * Проверка в момент [nowMs]; [started] — на снимке начало гнева. `true`, если [active] поменялся или это первая
     * проверка (поток слежения сообщает состояние сразу).
     */
    fun onCheck(nowMs: Long, started: Boolean): Boolean {
        if (started && (!active || untilMs - nowMs < restartMs)) untilMs = nowMs + remainingMs
        val now = nowMs < untilMs
        val changed = now != active || !checked
        active = now
        checked = true
        return changed
    }
}
