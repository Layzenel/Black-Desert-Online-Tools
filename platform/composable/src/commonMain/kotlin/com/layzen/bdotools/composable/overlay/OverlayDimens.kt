package com.layzen.bdotools.composable.overlay

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** Размеры окон поверх игры и меню оверлея. */
internal object OverlayDimens {
    val menuButton = 32.dp
    val menuGap = 6.dp
    val panelWidth = 420.dp
    /** Обычный размер оверлея комбо: от него кнопка меню по умолчанию встаёт слева. */
    val comboWindow = DpSize(960.dp, 300.dp)
}
