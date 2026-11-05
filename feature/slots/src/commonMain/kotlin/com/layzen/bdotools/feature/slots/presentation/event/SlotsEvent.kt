package com.layzen.bdotools.feature.slots.presentation.event

import com.layzen.bdotools.feature.slots.domain.BuiltInLayout
import com.layzen.bdotools.feature.slots.domain.GamePreset
import com.layzen.bdotools.feature.slots.domain.ScreenArea
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotPosition
import com.layzen.bdotools.feature.slots.presentation.state.GridStep

/** Действия пользователя на странице слотов. Сдвиги — в UI-px игры, а не в пикселях страницы. */
sealed interface SlotsEvent {
    // Папка и файл игры
    data object SelectFolder : SlotsEvent
    data object RequestPermission : SlotsEvent
    data class SelectAccount(val account: String) : SlotsEvent
    data object Reload : SlotsEvent
    data class SelectGamePreset(val preset: GamePreset) : SlotsEvent

    // Редактор
    data class Select(val id: SlotId, val additive: Boolean) : SlotsEvent
    data object ClearSelection : SlotsEvent

    /** Обводка рамкой: выбрать всё, что она задела; [additive] — добавить к уже выбранному. */
    data class SelectArea(val area: ScreenArea, val additive: Boolean) : SlotsEvent
    data class DragStart(val id: SlotId, val additive: Boolean) : SlotsEvent
    data class DragBy(val dx: Float, val dy: Float) : SlotsEvent
    data object DragEnd : SlotsEvent
    data class Nudge(val dx: Int, val dy: Int, val byGrid: Boolean) : SlotsEvent
    data class SetPosition(val id: SlotId, val position: SlotPosition) : SlotsEvent
    data object Undo : SlotsEvent
    data object Redo : SlotsEvent
    data class SetGrid(val step: GridStep) : SlotsEvent
    data class SetSnapStep(val step: Int) : SlotsEvent
    data class ToggleVisibility(val id: SlotId) : SlotsEvent

    // Пресеты
    data object UseGameLayout : SlotsEvent
    data class UseBuiltIn(val layout: BuiltInLayout) : SlotsEvent
    data class UseUserPreset(val name: String) : SlotsEvent
    data object OpenSaveAs : SlotsEvent
    data class SaveAsNameChanged(val name: String) : SlotsEvent
    data object ConfirmSaveAs : SlotsEvent
    data class RequestDelete(val name: String) : SlotsEvent
    data object ConfirmDelete : SlotsEvent
    data object OpenImport : SlotsEvent
    data class ImportTextChanged(val text: String) : SlotsEvent
    data object ImportFromFile : SlotsEvent
    data object ConfirmImport : SlotsEvent
    data object Export : SlotsEvent

    // Превью импорта и экспорта
    data class PreviewInclude(val ids: Set<SlotId>, val include: Boolean) : SlotsEvent
    /** Обводка рамкой в превью: отметить задетые элементы, а если все они уже отмечены — снять. */
    data class PreviewArea(val area: ScreenArea) : SlotsEvent
    data class PreviewScreen(val screen: ScreenSpace) : SlotsEvent
    data object PreviewUndo : SlotsEvent
    data object PreviewRedo : SlotsEvent
    data object ConfirmPreview : SlotsEvent

    // Запись в игру и подложка
    data object OpenApply : SlotsEvent
    data class SelectApplyTarget(val preset: GamePreset) : SlotsEvent
    data object ConfirmApply : SlotsEvent
    data object PickBackground : SlotsEvent
    data object ToggleBackground : SlotsEvent

    data object CloseDialog : SlotsEvent
    data object DismissNotice : SlotsEvent
}
