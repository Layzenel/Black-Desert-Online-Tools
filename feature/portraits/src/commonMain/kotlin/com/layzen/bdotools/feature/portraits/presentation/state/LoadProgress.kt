package com.layzen.bdotools.feature.portraits.presentation.state

/** Чтение портретов из папки игры: прочитано [done] из [total]. Сетка при этом показывает скелетоны. */
data class LoadProgress(val done: Int = 0, val total: Int = 0) {
    val fraction: Float get() = if (total > 0) done.toFloat() / total else 0f
}
