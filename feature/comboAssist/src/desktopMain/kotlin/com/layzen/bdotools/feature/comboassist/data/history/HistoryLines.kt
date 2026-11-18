package com.layzen.bdotools.feature.comboassist.data.history

/**
 * Когда в истории умений появилась новая строка — по тексту нижней и средней строк, кадр за кадром.
 *
 * Новое умение: игра сдвигает строки вверх, нижняя на 120–370 мс пустеет, а средняя остаётся; иконка нового умения
 * видна уже в эту паузу. После простоя история гаснет целиком (пустеют обе строки) и начинается заново с одной строки —
 * тогда новая строка — это появление текста снизу. Пока строка открыта и умение не узнано, иконку сравнивают на каждом
 * кадре; через [timeoutMs] — сдаются. Первый кадр только запоминается: строка, которая уже была на экране, — не новая.
 *
 * @property timeoutMs сколько пытаться узнать иконку новой строки
 */
internal class HistoryLines(private val timeoutMs: Long = DEFAULT_TIMEOUT_MS) {
    private var started = false
    private var bottomShown = false
    private var open = false
    private var recognized = false
    private var openedAtMs = 0L

    /** Что делать с кадром, где нижняя строка [bottom] и средняя [middle] с текстом (или нет), в момент [nowMs]. */
    fun onFrame(bottom: Boolean, middle: Boolean, nowMs: Long): Step {
        if (!started) { started = true; bottomShown = bottom; return Step.Idle }
        val opened = when {
            bottomShown && !bottom && middle -> true // сдвиг строк
            bottomShown && !bottom -> { open = false; false } // всё погасло
            !bottomShown && bottom && !open -> true // первая строка после простоя
            else -> false
        }
        bottomShown = bottom
        if (opened) { open = true; recognized = false; openedAtMs = nowMs; return Step.Try(shift = middle) }
        if (!open || recognized) return Step.Idle
        if (nowMs - openedAtMs > timeoutMs) { open = false; return Step.GaveUp }
        return Step.Try(shift = false)
    }

    /** Умение новой строки узнано: до следующей строки больше не сравнивать. */
    fun recognized() { recognized = true }

    /** Решение по кадру. */
    sealed interface Step {
        /** Ничего не делать. */
        data object Idle : Step

        /** Сравнить иконку; [shift] — строка только что открылась сдвигом строк (для журнала). */
        data class Try(val shift: Boolean) : Step

        /** Строку так и не узнали за отведённое время. */
        data object GaveUp : Step
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 800L
    }
}
