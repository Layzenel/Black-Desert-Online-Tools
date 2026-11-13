package com.layzen.bdotools.feature.uieditor.presentation.ui.sidebar

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_game_settings
import com.layzen.bdotools.feature.uieditor.resources.slots_view
import com.layzen.bdotools.uikit.components.SectionDivider
import com.layzen.bdotools.uikit.components.SectionHeader
import org.jetbrains.compose.resources.stringResource

/**
 * Меню слева (аккаунт выбирается в верхней навигации), сверху вниз — от того, откуда раскладка, к тому, как она видна:
 * - источник: «Пресеты в игре», «Готовые пресеты», «Мои пресеты»;
 * - персонаж, чьи навыки видны в слотах перезарядки, — сразу под пресетами;
 * - настройки как в игре: «Настройка быстрых слотов» и «Разделение HP»;
 * - вид: сетка, прозрачность слотов с навыком и подложка;
 * - выбранный элемент — внизу, под настройками.
 * Прокрутку и отступы даёт страница `ToolPage`.
 */
@Composable
internal fun SlotsSidebar(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    GamePresetSettings(state, onEvent)
    PresetList(state, onEvent)
    if (state.skills.characters.isNotEmpty()) CharacterPicker(state.skills, onEvent)
    if (state.loaded != null) {
        SectionDivider()
        SectionHeader(stringResource(Res.string.slots_game_settings))
        QuickSlotSettings(state.layout.customQuickSlots) { onEvent(SlotsEvent.SetCustomQuickSlots(it)) }
        SplitHpSettings(state.layout.splitHp) { onEvent(SlotsEvent.SetSplitHp(it)) }
    }
    SectionDivider()
    SectionHeader(stringResource(Res.string.slots_view))
    GridSettings(state, onEvent)
    if (state.skills.selected != null) SkillFrameTransparency(state.skillFrameTransparency, onEvent)
    BackgroundSettings(state, onEvent)
    SectionDivider()
    SlotInspector(state, onEvent)
}
