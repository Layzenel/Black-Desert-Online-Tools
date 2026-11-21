package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.skill.domain.SkillDetails
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.presentation.event.ComboEvent
import com.layzen.bdotools.feature.comboassist.presentation.state.ComboState
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.skillName
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_add_lane_action
import com.layzen.bdotools.feature.comboassist.resources.combo_cancel_selection
import com.layzen.bdotools.feature.comboassist.resources.combo_cell
import com.layzen.bdotools.feature.comboassist.resources.combo_cell_empty
import com.layzen.bdotools.feature.comboassist.resources.combo_chain
import com.layzen.bdotools.feature.comboassist.resources.combo_chain_description
import com.layzen.bdotools.feature.comboassist.resources.combo_insert_left
import com.layzen.bdotools.feature.comboassist.resources.combo_insert_right
import com.layzen.bdotools.feature.comboassist.resources.combo_make_fork
import com.layzen.bdotools.feature.comboassist.resources.combo_note
import com.layzen.bdotools.feature.comboassist.resources.combo_note_description
import com.layzen.bdotools.feature.comboassist.resources.combo_note_placeholder
import com.layzen.bdotools.feature.comboassist.resources.combo_remove
import com.layzen.bdotools.feature.comboassist.resources.combo_seen_in_game
import com.layzen.bdotools.feature.comboassist.resources.combo_seen_in_game_hint
import com.layzen.bdotools.feature.comboassist.resources.menu_add
import com.layzen.bdotools.feature.comboassist.resources.menu_cancel
import com.layzen.bdotools.feature.comboassist.resources.menu_delete
import com.layzen.bdotools.feature.comboassist.resources.menu_fork
import com.layzen.bdotools.feature.comboassist.resources.menu_insert_left
import com.layzen.bdotools.feature.comboassist.resources.menu_insert_right
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.components.ToolbarButton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Свойства выбранной ячейки: вставить рядом, развилка, связка, видно ли умение в истории или на слоте, команда активации умения, пометка для трекера, снять выбор, удалить. Кнопки — иконками с подсказками. */
@Composable
internal fun CellPanel(state: ComboState, onEvent: (ComboEvent) -> Unit, loadDetails: suspend (Int) -> SkillDetails?) {
    val combo = state.edited ?: return
    val at = state.selection ?: return
    val cell = combo[at] ?: return
    val column = combo.columns[at.column]
    Column(Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        SectionHeader(
            stringResource(Res.string.combo_cell, combo.label(at)),
            description = cell.skillNo?.let { skillName(state.skills, it) } ?: stringResource(Res.string.combo_cell_empty),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
            IconActionButton(Res.string.combo_insert_left, Res.drawable.menu_insert_left) { onEvent(ComboEvent.InsertCell(after = false)) }
            IconActionButton(Res.string.combo_insert_right, Res.drawable.menu_insert_right) { onEvent(ComboEvent.InsertCell(after = true)) }
            when (column) {
                is ComboColumn.Single -> IconActionButton(Res.string.combo_make_fork, Res.drawable.menu_fork) { onEvent(ComboEvent.ToFork(at.column)) }
                is ComboColumn.Fork -> {
                    IconActionButton(Res.string.combo_add_lane_action, Res.drawable.menu_add) { onEvent(ComboEvent.AddLane(at.column)) }
                }
            }
            IconActionButton(Res.string.combo_cancel_selection, Res.drawable.menu_cancel) { onEvent(ComboEvent.ClearSelection) }
            IconActionButton(Res.string.combo_remove, Res.drawable.menu_delete) { onEvent(ComboEvent.RemoveCell) }
        }
        Column(Modifier.widthIn(max = ComboDimens.panelWidth), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            HoverTooltip(stringResource(Res.string.combo_chain_description)) {
                ToggleSwitch(cell.chain, { onEvent(ComboEvent.ToggleChain) }, label = stringResource(Res.string.combo_chain))
            }
            cell.skillNo?.let { skillNo ->
                HoverTooltip(stringResource(Res.string.combo_seen_in_game_hint)) {
                    ToggleSwitch(skillNo !in combo.keyTracked, { onEvent(ComboEvent.ToggleKeyTracked) }, label = stringResource(Res.string.combo_seen_in_game))
                }
                ActivationPicker(skillNo, cell.keys, onEvent, loadDetails)
            }
            SectionHeader(stringResource(Res.string.combo_note), description = stringResource(Res.string.combo_note_description))
            InputField(cell.note, { onEvent(ComboEvent.SetNote(it)) }, Modifier.fillMaxWidth(), placeholder = stringResource(Res.string.combo_note_placeholder))
        }
    }
}

/** Кнопка-иконка; подпись [text] — подсказкой при наведении. */
@Composable
private fun IconActionButton(text: StringResource, icon: DrawableResource, onClick: () -> Unit) {
    ToolbarButton(painterResource(icon), stringResource(text), onClick)
}
