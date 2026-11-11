package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/** Чтение портретов из папки игры: прочитано [done] из [total]. Сетка при этом показывает скелетоны. */
data class LoadProgress(val done: Int = 0, val total: Int = 0) {
    /** Доля прочитанного от 0 до 1. */
    val fraction: Float get() = if (total > 0) done.toFloat() / total else 0f
}
