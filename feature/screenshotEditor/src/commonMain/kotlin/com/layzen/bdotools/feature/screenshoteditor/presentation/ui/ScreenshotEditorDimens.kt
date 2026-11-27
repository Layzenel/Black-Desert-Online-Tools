package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.unit.dp

/** Размеры редактора скриншотов. */
internal object ScreenshotEditorDimens {
    /** Рамка обрезки поверх скриншота — в точках экрана, при любом масштабе картинки. */
    val cropBorder = 2.dp

    /** Наименьшая ширина плитки в галерее: столбцов столько, сколько таких влезает. */
    val galleryTile = 240.dp

    /** Ширина уведомления о новом скриншоте поверх игры. */
    val alertWidth = 260.dp
}
