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

    /** Клик по окну чата [window]: выбрать его; [additive] (Ctrl) — добавить к выбору или убрать из него. */
    data class SelectChat(val window: Int, val additive: Boolean) : SlotsEvent

    /** Окно чата [window] схватили: дальше [DragBy] двигает его вместе со всем выбранным, конец — [DragEnd]. */
    data class DragChatStart(val window: Int, val additive: Boolean) : SlotsEvent
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

    /** Шаг растягивания окна чата [window] за правый верхний угол: сдвиг угла от начала жеста. */
    data class ResizeChat(val window: Int, val dx: Int, val dy: Int) : SlotsEvent
    data class SetChatSize(val window: Int, val width: Int, val height: Int) : SlotsEvent

    /** Центр окна чата [window] от центра экрана, введённый руками. */
    data class SetChatPosition(val window: Int, val center: SlotPosition) : SlotsEvent

    /** Шаг слайдера «Отдаление»: группы выбранного на [value] UI-px от центра между ними по оси. */
    data class SpreadGroups(val horizontal: Boolean, val value: Int) : SlotsEvent

    /** Слайдер «Отдаление» отпустили: прежняя раскладка уходит в «Отменить» одним шагом. */
    data object SpreadGroupsEnd : SlotsEvent

    /** Перетаскивание или растягивание окна чата закончилось: прежняя раскладка уходит в «Отменить». */
    data object ChatEditEnd : SlotsEvent
    data class ToggleChatVisibility(val window: Int) : SlotsEvent

    /** Вкладку главного окна [window] — в отдельное окно над главным. */
    data class DetachChat(val window: Int) : SlotsEvent

    /** Отдельное окно чата [window] — во вкладку главного окна. */
    data class CombineChat(val window: Int) : SlotsEvent
    data class SetDistributeGap(val gap: Int, val horizontal: Boolean) : SlotsEvent
    data class ToggleVisibility(val id: SlotId) : SlotsEvent

    /** Заглушка надписи под слотом шириной [chars] символов; `null` — убрать. */
    data class SetLabel(val id: SlotId, val chars: Int?) : SlotsEvent

    /** «Настройка быстрых слотов»: включена — 20 слотов хотбара, выключена — одна панель «Быстрый слот». */
    data class SetCustomQuickSlots(val enabled: Boolean) : SlotsEvent

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
    /** Отметить или снять в превью элементы [ids] и окна чата [chats] (по `WindowIndex`) — одним шагом «Отменить». */
    data class PreviewInclude(val ids: Set<SlotId>, val include: Boolean, val chats: Set<Int> = emptySet()) : SlotsEvent
    /** Обводка рамкой в превью: отметить задетые элементы, а если все они уже отмечены — снять. */
    data class PreviewArea(val area: ScreenArea) : SlotsEvent
    data class PreviewScreen(val screen: ScreenSpace) : SlotsEvent

    /** «Настройка быстрых слотов» раскладки в превью: она и уйдёт в редактор или файл. */
    data class PreviewCustomQuickSlots(val enabled: Boolean) : SlotsEvent
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
