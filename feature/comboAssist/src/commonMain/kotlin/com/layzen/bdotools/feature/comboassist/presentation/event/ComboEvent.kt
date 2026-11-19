package com.layzen.bdotools.feature.comboassist.presentation.event

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.skill.domain.SkillStance
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboMode

/** Действия игрока на экране комбо. */
sealed interface ComboEvent {
    data class SelectCombo(val id: String) : ComboEvent
    data object NewCombo : ComboEvent
    data class Rename(val name: String) : ComboEvent
    data class SelectClass(val characterClass: CharacterClass) : ComboEvent
    data class SelectStance(val stance: SkillStance?) : ComboEvent
    data class Query(val text: String) : ComboEvent
    data object Save : ComboEvent
    data object Delete : ComboEvent
    data object ExportCombo : ComboEvent
    data object ImportCombos : ComboEvent

    data class SelectCell(val at: CellAt) : ComboEvent
    data class Fill(val skillNo: Int) : ComboEvent
    data class InsertColumn(val index: Int) : ComboEvent
    data class ToFork(val column: Int) : ComboEvent
    data class AddLane(val column: Int) : ComboEvent
    data class ToggleLinked(val column: Int) : ComboEvent
    data class GrowLane(val column: Int, val lane: Int) : ComboEvent
    data class InsertCell(val after: Boolean) : ComboEvent
    data object ClearSelection : ComboEvent
    data object ToggleChain : ComboEvent
    data object ToggleOptional : ComboEvent
    data class SetNote(val text: String) : ComboEvent
    data object RemoveCell : ComboEvent
    data class RemoveAt(val at: CellAt) : ComboEvent
    data class SwapCells(val from: CellAt, val to: CellAt) : ComboEvent
    data class AddFiller(val skillNo: Int) : ComboEvent
    data class RemoveFiller(val skillNo: Int) : ComboEvent

    data class SetMode(val mode: ComboMode) : ComboEvent
    data class Press(val skillNo: Int) : ComboEvent
    data object ToggleTrackerMove : ComboEvent
    data object ToggleSchemaMove : ComboEvent
    data object ToggleSchema : ComboEvent
    data object ToggleVertical : ComboEvent
    data class SetOverlayStyle(val style: OverlayStyle) : ComboEvent

    /** Из меню оверлея: открыть комбо [id] в оверлее и сразу следить за игрой. */
    data class ShowCombo(val id: String) : ComboEvent

    /** Из меню оверлея: включить или выключить тултипы над слотами перезарядки. */
    data object ToggleSlotTooltips : ComboEvent
    data object ToggleSlotShields : ComboEvent

    /** Из меню оверлея: размер оверлея комбо в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetComboScale(val percent: Int, val save: Boolean) : ComboEvent

    /** Из меню оверлея: непрозрачность оверлея комбо в процентах; [save] — закончили двигать ползунок, сохранить. */
    data class SetComboOpacity(val percent: Int, val save: Boolean) : ComboEvent

    /** Из меню оверлея: убрать оверлей; слежение за игрой продолжается. */
    data object HideCombo : ComboEvent
}
