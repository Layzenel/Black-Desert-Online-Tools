package com.layzen.bdotools.feature.comboassist.presentation.event

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.feature.comboassist.domain.skill.ComboStance
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.DropSide
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboMode

/** Действия игрока на экране комбо. */
sealed interface ComboEvent {
    data class SelectCombo(val id: String) : ComboEvent
    data object NewCombo : ComboEvent
    data class Rename(val name: String) : ComboEvent
    data class SelectClass(val characterClass: CharacterClass) : ComboEvent
    data class SelectStance(val stance: ComboStance?) : ComboEvent
    data class Query(val text: String) : ComboEvent
    data object Save : ComboEvent

    /** Спросить, удалять ли открытое комбо: само удаление — [Delete] из окна подтверждения. */
    data object AskDelete : ComboEvent

    /** Не удалять: закрыть окно подтверждения. */
    data object CancelDelete : ComboEvent
    data object Delete : ComboEvent

    /** Ctrl+Z: вернуть комбо, каким оно было до последней правки. */
    data object Undo : ComboEvent
    data object Redo : ComboEvent
    data object ExportCombo : ComboEvent
    data object ImportCombos : ComboEvent
    data class EditRage(val rage: Boolean) : ComboEvent
    data object AddRage : ComboEvent
    data object RemoveRage : ComboEvent

    /** Скопировать обычное комбо в комбо под гневом: шаги, филлеры и опциональные умения — чтобы поменять только отличия. */
    data object CopyToRage : ComboEvent

    data class SelectCell(val at: CellAt) : ComboEvent
    data class Fill(val skillNo: Int) : ComboEvent
    data class InsertColumn(val index: Int) : ComboEvent

    /** Умение [skillNo] из списка бросили на холст у ячейки [at] со стороны [side]: оно встаёт туда новой ячейкой. */
    data class DropSkill(val skillNo: Int, val at: CellAt, val side: DropSide) : ComboEvent
    data class ToFork(val column: Int) : ComboEvent
    data class AddLane(val column: Int) : ComboEvent
    data class GrowLane(val column: Int, val lane: Int) : ComboEvent
    data class InsertCell(val after: Boolean) : ComboEvent
    data object ClearSelection : ComboEvent
    data object ToggleChain : ComboEvent

    /** Переключить у умения выбранной ячейки, видно ли оно в истории или на слоте ([com.layzen.bdotools.feature.comboassist.domain.combo.Combo.keyTracked]). */
    data object ToggleKeyTracked : ComboEvent

    /** Команда активации умения в выбранной ячейке — действия игры, зажатые вместе; `null` — любой вариант команды. */
    data class SetKeyVariant(val actions: Set<String>?) : ComboEvent
    data class SetNote(val text: String) : ComboEvent
    data object RemoveCell : ComboEvent
    data class RemoveAt(val at: CellAt) : ComboEvent
    data class MoveCell(val from: CellAt, val to: CellAt, val side: DropSide) : ComboEvent
    data class AddFiller(val skillNo: Int) : ComboEvent
    data class AddPool(val skillNo: Int) : ComboEvent
    data class RemovePool(val skillNo: Int) : ComboEvent

    /** Сдвинуть опциональное умение [skillNo] на [by] мест: меньше нуля — левее, больше — правее; за краем — крайним. */
    data class MovePool(val skillNo: Int, val by: Int) : ComboEvent
    data class RemoveFiller(val skillNo: Int) : ComboEvent

    data class SetMode(val mode: ComboMode) : ComboEvent
    data class Press(val skillNo: Int) : ComboEvent
    data object ToggleSchema : ComboEvent

    /** Из меню оверлея: показывать под умениями схемы клавиши их применения. */
    data object ToggleSchemaKeys : ComboEvent

    /** Показать окно «Как работает трекер». */
    data object ShowTrackerNotice : ComboEvent

    /** Закрыть окно «Как работает трекер»: при следующем открытии вкладки оно само больше не появится. */
    data object CloseTrackerNotice : ComboEvent
    data object ToggleVertical : ComboEvent
    data class SetOverlayStyle(val style: OverlayStyle) : ComboEvent

    /** Из меню оверлея: открыть комбо [id] в оверлее и сразу следить за игрой. */
    data class ShowCombo(val id: String) : ComboEvent

    /** Из меню оверлея: включить или выключить тултипы над слотами перезарядки. */
    data object ToggleSlotTooltips : ComboEvent
    data object ToggleSlotShields : ComboEvent
    data object ToggleSlotControls : ComboEvent

    /** Из меню оверлея: размер оверлея комбо в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetComboScale(val percent: Int, val save: Boolean) : ComboEvent

    /** Из меню оверлея: непрозрачность оверлея комбо в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetComboOpacity(val percent: Int, val save: Boolean) : ComboEvent

    /** Через сколько секунд без умений комбо трекер начинает его сначала. */
    data class SetResetSeconds(val seconds: Int, val save: Boolean) : ComboEvent

    /** Из меню оверлея: убрать оверлей; слежение за игрой продолжается. */
    data object HideCombo : ComboEvent
}
