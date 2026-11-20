package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable

/** Что вкладка меню оверлея может сделать с приложением. */
interface OverlayScope {
    /** Развернуть программу на вкладке фичи ([OverlayContribution.id]) и закрыть меню. */
    fun openApp()

    /**
     * Показать [content] рядом с меню, со стороны центра экрана (например, схему комбо под курсором), или убрать
     * его (`null`).
     */
    fun aside(content: (@Composable () -> Unit)?)
}
