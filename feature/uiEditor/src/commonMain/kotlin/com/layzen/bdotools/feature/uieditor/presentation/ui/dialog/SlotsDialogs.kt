package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.title
import com.layzen.bdotools.feature.uieditor.presentation.ui.preview.LayoutPreviewDialog
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_delete_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_delete_description
import com.layzen.bdotools.feature.uieditor.resources.slots_delete_title
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_names_language
import com.layzen.bdotools.feature.uieditor.resources.slots_skill_number
import com.layzen.bdotools.feature.uieditor.resources.slots_undo_apply_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_undo_apply_description
import com.layzen.bdotools.feature.uieditor.resources.slots_undo_apply_title
import org.jetbrains.compose.resources.stringResource

/** Открытое окно страницы, если оно есть. */
@Composable
internal fun SlotsDialogs(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        is SlotsDialog.SaveAs -> SaveAsDialog(dialog, state.presets.map { it.name }, onEvent)
        is SlotsDialog.Import -> ImportDialog(dialog, onEvent)
        is SlotsDialog.Preview -> state.loaded?.game?.screen?.let {
            LayoutPreviewDialog(dialog, state.skills, it, state.background, state.backgroundShown, state.backgroundDim, onEvent)
        }
        is SlotsDialog.ConfirmApply -> ApplyDialog(dialog, state.loaded?.presets?.keys?.sorted().orEmpty(), state.layout, state.skills, onEvent)
        is SlotsDialog.EditMemo -> {
            val language = stringResource(Res.string.slots_skill_names_language)
            val name = state.skills.skill(dialog.skillNo)?.name(language) ?: stringResource(Res.string.slots_skill_number, dialog.skillNo)
            MemoDialog(dialog, name, onEvent)
        }
        is SlotsDialog.PickSkill -> {
            val slotted = state.layout.skills.orEmpty().filterKeys { it.group == SlotGroup.Cooldown }.entries.associate { (id, skillNo) -> skillNo to id.number }
            SkillPickerDialog(dialog, state.skills, slotted, onEvent)
        }
        is SlotsDialog.ImportCharacter -> ImportCharacterDialog(dialog, state.skills, onEvent)
        is SlotsDialog.ConfirmDelete -> ConfirmDialog(
            title = stringResource(Res.string.slots_delete_title),
            text = stringResource(Res.string.slots_delete_description, dialog.name),
            confirm = stringResource(Res.string.slots_delete_confirm),
            onCancel = close,
            onConfirm = { onEvent(SlotsEvent.ConfirmDelete) },
        )
        SlotsDialog.ConfirmUndoApply -> ConfirmDialog(
            title = stringResource(Res.string.slots_undo_apply_title),
            text = stringResource(Res.string.slots_undo_apply_description),
            confirm = stringResource(Res.string.slots_undo_apply_confirm),
            onCancel = close,
            onConfirm = { onEvent(SlotsEvent.ConfirmUndoApply) },
        )
    }
}
