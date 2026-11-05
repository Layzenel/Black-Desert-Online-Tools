package com.layzen.bdotools.feature.slots.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.uikit.components.SectionDivider

/**
 * Меню слева (аккаунт выбирается в верхней навигации): набор позиций игры, пресеты, сетка, подложка и координаты выбранного слота.
 * Прокрутку и отступы даёт страница `ToolPage`.
 */
@Composable
internal fun SlotsSidebar(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    GamePresetSettings(state, onEvent)
    SectionDivider()
    PresetList(state, onEvent)
    SectionDivider()
    GridSettings(state, onEvent)
    SectionDivider()
    BackgroundSettings(state, onEvent)
    SectionDivider()
    SlotInspector(state, onEvent)
}
