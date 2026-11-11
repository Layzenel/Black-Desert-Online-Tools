package com.layzen.bdotools.feature.uieditor.presentation.ui

import androidx.compose.ui.unit.dp

internal object SlotsDimens {
    val sidebarWidth = 280.dp
    val snapSliderWidth = 70.dp
    val guideStroke = 1.dp
    val coordinateFieldWidth = 96.dp
    val dialogWidth = 520.dp
    val importTextHeight = 220.dp
    val previewSidebarWidth = 300.dp
    val previewNumberCellWidth = 48.dp
    val previewItemIndent = 16.dp
    val marqueeBorder = 1.dp
    val legendSwatch = 10.dp
    val slotBorder = 1.dp
    val selectedSlotBorder = 2.dp
    val gridLine = 1.dp
    val crosshairStroke = 1.5.dp
    val eyeStroke = 1.dp
    val labelMinIconWidth = 18.dp
    val chatHandle = 8.dp
    val helpDialogMaxWidth = 640.dp
    val helpMaxHeight = 560.dp
    val chatTabIcon = 14.dp
    val chatTabPaddingVertical = 2.dp
    val portraitWidth = 40.dp
    val portraitHeight = 52.dp
    val skillIcon = 32.dp
    val skillListHeight = 420.dp
    val skillNumberPadding = 2.dp
    val classIcon = 22.dp
    val stanceSelectWidth = 160.dp
    val skillTooltipWidth = 380.dp
    val skillTooltipIcon = 44.dp
    const val GRID_ALPHA = 0.12f
    const val GRID_MAJOR_ALPHA = 0.22f
    const val AXIS_ALPHA = 0.7f
    const val CROSSHAIR_ALPHA = 0.85f
    const val SLOT_FILL_ALPHA = 0.55f
    const val FIXED_FILL_ALPHA = 0.35f
    const val HIDDEN_ALPHA = 0.35f
    const val MARQUEE_FILL_ALPHA = 0.12f
    const val LABEL_FILL_ALPHA = 0.3f

    /** Затемнение подложки номера слота поверх иконки навыка. */
    const val SKILL_NUMBER_BACKDROP_ALPHA = 0.65f

    /** Прозрачность иконки навыка, который тащат между слотами. */
    const val SKILL_DRAG_ALPHA = 0.8f

    /** Какую долю окна приложения занимает превью импорта и экспорта. */
    const val PREVIEW_WIDTH_SHARE = 0.94f
    const val PREVIEW_HEIGHT_SHARE = 0.92f

    /** Размер глазика — доля ширины иконки: он растёт вместе с увеличением холста. */
    const val EYE_SHARE = 0.32f

    /** Отступ глазика от верхнего края рамки — доля его размера. */
    const val EYE_TOP_SHARE = 0.25f

    /** Каждая какая линия сетки толще: 5 — для мелкого шага, 2 — для шага игры. */
    const val MAJOR_EVERY_FINE = 5
    const val MAJOR_EVERY_COARSE = 2

    /** Высота уменьшенного портрета в памяти, px: вдвое больше показанного, чтобы был чётким на экранах с масштабом. */
    const val PORTRAIT_THUMBNAIL_HEIGHT = 104

    /** Высота букв надписи под слотом — доля высоты её полосы. */
    const val MEMO_TEXT_SHARE = 0.85f
}
