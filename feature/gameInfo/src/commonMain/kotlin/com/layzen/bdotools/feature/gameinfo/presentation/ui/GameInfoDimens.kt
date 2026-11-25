package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** Размеры пинга и сетки поверх игры. */
internal object GameInfoDimens {
    val pingIcon = 20.dp
    val pingBarGap = 2.dp
    val frame = 2.dp
    val pingWindow = DpSize(140.dp, 48.dp)
    val frameGraphHeight = 28.dp
    val frameGraphLine = 1.dp
    val gridLine = 1.dp

    /** Прозрачность обычной линии сетки, каждой [MAJOR_EVERY] линии и осей через центр экрана. */
    const val GRID_ALPHA = 0.12f
    const val GRID_MAJOR_ALPHA = 0.22f
    const val GRID_AXIS_ALPHA = 0.7f

    /** Каждая какая по счёту линия — заметная: у сетки 50 — каждая вторая, у мелких — каждая пятая. */
    const val MAJOR_EVERY_FINE = 5
    const val MAJOR_EVERY_COARSE = 2
}
