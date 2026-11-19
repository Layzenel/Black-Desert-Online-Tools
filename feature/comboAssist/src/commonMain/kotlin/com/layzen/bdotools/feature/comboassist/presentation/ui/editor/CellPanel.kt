package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.layzen.bdotools.feature.comboassist.resources.combo_optional
import com.layzen.bdotools.feature.comboassist.resources.combo_optional_description
import com.layzen.bdotools.feature.comboassist.resources.combo_remove
import com.layzen.bdotools.feature.comboassist.resources.menu_add
import com.layzen.bdotools.feature.comboassist.resources.menu_cancel
import com.layzen.bdotools.feature.comboassist.resources.menu_delete
import com.layzen.bdotools.feature.comboassist.resources.menu_fork
import com.layzen.bdotools.feature.comboassist.resources.menu_insert_left
import com.layzen.bdotools.feature.comboassist.resources.menu_insert_right
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Свойства выбранной ячейки: вставить рядом, развилка, связка, опциональность, пометка для трекера, снять выбор, удалить. */
@Composable
internal fun CellPanel(state: ComboState, onEvent: (ComboEvent) -> Unit) {
    val combo = state.combo ?: return
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
            ToggleSwitch(cell.chain, { onEvent(ComboEvent.ToggleChain) }, label = stringResource(Res.string.combo_chain), description = stringResource(Res.string.combo_chain_description))
            ToggleSwitch(cell.optional, { onEvent(ComboEvent.ToggleOptional) }, label = stringResource(Res.string.combo_optional), description = stringResource(Res.string.combo_optional_description))
            SectionHeader(stringResource(Res.string.combo_note), description = stringResource(Res.string.combo_note_description))
            InputField(cell.note, { onEvent(ComboEvent.SetNote(it)) }, Modifier.fillMaxWidth(), placeholder = stringResource(Res.string.combo_note_placeholder))
        }
    }
}

/** Кнопка с иконкой слева от подписи. */
@Composable
private fun IconActionButton(text: StringResource, icon: DrawableResource, onClick: () -> Unit) {
    ActionButton(onClick = onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(icon), null, Modifier.size(ComboDimens.buttonIcon))
            Text(stringResource(text), style = AppTheme.typography.button, color = AppTheme.colors.textButton)
        }
    }
}
