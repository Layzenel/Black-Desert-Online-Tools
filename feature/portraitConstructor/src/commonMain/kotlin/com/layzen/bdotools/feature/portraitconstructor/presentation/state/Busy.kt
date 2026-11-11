package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/** Долгая операция: показывается окном ожидания с прогрессом [done] из [total]. */
data class Busy(val kind: BusyKind, val done: Int = 0, val total: Int = 0) {
    /** Доля выполнения от 0 до 1; `null`, пока [total] неизвестен. */
    val progress: Float? get() = if (total > 0) done.toFloat() / total else null
}
