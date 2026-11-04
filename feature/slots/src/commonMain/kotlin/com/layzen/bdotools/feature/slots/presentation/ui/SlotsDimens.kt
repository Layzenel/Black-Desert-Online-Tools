package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.ui.unit.dp

internal object SlotsDimens {
    val windowMaxWidth = 1440.dp
    val sidebarWidth = 280.dp
    val sidebarMaxHeight = 760.dp
    val coordinateFieldWidth = 96.dp
    val dialogWidth = 520.dp
    val importTextHeight = 220.dp
    val legendSwatch = 10.dp
    val slotBorder = 1.dp
    val selectedSlotBorder = 2.dp
    val gridLine = 1.dp
    const val GRID_ALPHA = 0.12f
    const val GRID_MAJOR_ALPHA = 0.22f
    const val AXIS_ALPHA = 0.7f
    const val SLOT_FILL_ALPHA = 0.55f

    /** Каждая какая линия сетки толще: 5 — для мелкого шага, 2 — для шага игры. */
    const val MAJOR_EVERY_FINE = 5
    const val MAJOR_EVERY_COARSE = 2
}
