package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.unit.dp

/** Размеры редактора скриншотов. */
internal object ScreenshotEditorDimens {
    /** Меню слева — как у редактора интерфейса и помощника комбо. */
    val sidebarWidth = 280.dp

    /** Рамка обрезки и выбора поверх скриншота — в точках экрана, при любом масштабе картинки. */
    val cropBorder = 2.dp

    /** Штрих и промежуток пунктира вокруг выбранной правки. */
    val selectionDash = 6.dp
    val selectionGap = 4.dp

    /** Запас вокруг правки, в пределах которого клик её выбирает. */
    val hitTolerance = 6.dp

    /** Кнопки у правки и обрезки на холсте: сторона и отступ от рамки. */
    val canvasButton = 28.dp
    val canvasButtonIcon = 18.dp
    val canvasButtonGap = 6.dp

    /** Окно «Закрыть скриншот?» — как подтверждения в редакторе интерфейса. */
    val dialogWidth = 520.dp

    /** Наименьшая ширина плитки в галерее: столбцов столько, сколько таких влезает. */
    val galleryTile = 240.dp

    /** Ширина уведомления о новом скриншоте поверх игры. */
    val alertWidth = 260.dp

    /** Окно быстрой обрезки поверх игры. */
    val quickCropWidth = 960.dp
    val quickCropHeight = 600.dp
}
