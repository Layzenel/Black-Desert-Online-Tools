package com.layzen.bdotools.feature.uieditor.presentation.event

import com.layzen.bdotools.feature.uieditor.domain.AlignEdge
import com.layzen.bdotools.feature.uieditor.domain.BuiltInLayout
import com.layzen.bdotools.feature.uieditor.domain.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.ScreenArea
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.SlotId
import com.layzen.bdotools.feature.uieditor.domain.SlotPosition
import com.layzen.bdotools.feature.uieditor.presentation.state.GridStep

/** Действия пользователя на странице слотов. Сдвиги — в UI-px игры, а не в пикселях страницы. */
sealed interface SlotsEvent {
    // Папка и файл игры
    data object SelectFolder : SlotsEvent
    data object RequestPermission : SlotsEvent
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
    data class SetSnapToGrid(val enabled: Boolean) : SlotsEvent
    data class SetSnapStep(val step: Int) : SlotsEvent
    data class SetSnapToSlots(val enabled: Boolean) : SlotsEvent
    data class SetSnapDistance(val distance: Int) : SlotsEvent
    /** [anchor] — опорный элемент (по нему открыли меню), остальные встают по нему; `null` — по рамке всех выбранных. */
    data class Align(val edge: AlignEdge, val anchor: SlotId? = null) : SlotsEvent
    data class Distribute(val horizontal: Boolean) : SlotsEvent
    data class SetDistributeGap(val gap: Int) : SlotsEvent
    data class ToggleVisibility(val id: SlotId) : SlotsEvent

    /** Заглушка надписи под слотом шириной [chars] символов; `null` — убрать. */
    data class SetLabel(val id: SlotId, val chars: Int?) : SlotsEvent

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

    /** Затенение подложки в процентах: 0 — картинка как есть. */
    data class SetBackgroundDim(val percent: Int) : SlotsEvent

    data object CloseDialog : SlotsEvent
    data object DismissNotice : SlotsEvent
}
