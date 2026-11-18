package com.layzen.bdotools.composable.overlay

/** Что вкладка меню оверлея может сделать с приложением. */
interface OverlayScope {
    /** Развернуть программу на вкладке фичи ([OverlayContribution.id]) и закрыть меню. */
    fun openApp()
}
