package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.uikit.components.SectionDivider

/**
 * Меню слева (аккаунт выбирается в верхней навигации): списки «Пресеты в игре», «Готовые пресеты», «Мои пресеты»;
 * «Настройка быстрых слотов», «Разделение HP» и сетка, подложка и координаты выбранного.
 * Прокрутку и отступы даёт страница `ToolPage`.
 */
@Composable
internal fun SlotsSidebar(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    GamePresetSettings(state, onEvent)
    PresetList(state, onEvent)
    SectionDivider()
    if (state.loaded != null) {
        QuickSlotSettings(state.layout.customQuickSlots) { onEvent(SlotsEvent.SetCustomQuickSlots(it)) }
        SplitHpSettings(state.layout.splitHp) { onEvent(SlotsEvent.SetSplitHp(it)) }
    }
    GridSettings(state, onEvent)
    SectionDivider()
    BackgroundSettings(state, onEvent)
    SectionDivider()
    SlotInspector(state, onEvent)
}
