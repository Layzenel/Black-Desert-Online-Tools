package com.layzen.bdotools.feature.slots.presentation.ui.dialog

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_apply_confirm
import com.layzen.bdotools.feature.slots.resources.slots_apply_description
import com.layzen.bdotools.feature.slots.resources.slots_apply_title
import com.layzen.bdotools.feature.slots.resources.slots_delete_confirm
import com.layzen.bdotools.feature.slots.resources.slots_delete_description
import com.layzen.bdotools.feature.slots.resources.slots_delete_title
import org.jetbrains.compose.resources.stringResource

/** Открытое окно страницы, если оно есть. */
@Composable
internal fun SlotsDialogs(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        is SlotsDialog.SaveAs -> SaveAsDialog(dialog, onEvent)
        is SlotsDialog.Import -> ImportDialog(dialog, onEvent)
        SlotsDialog.ConfirmApply -> ConfirmDialog(
            title = stringResource(Res.string.slots_apply_title),
            text = stringResource(Res.string.slots_apply_description),
            confirm = stringResource(Res.string.slots_apply_confirm),
            onCancel = close,
            onConfirm = { onEvent(SlotsEvent.ConfirmApply) },
        )
        is SlotsDialog.ConfirmDelete -> ConfirmDialog(
            title = stringResource(Res.string.slots_delete_title),
            text = stringResource(Res.string.slots_delete_description, dialog.name),
            confirm = stringResource(Res.string.slots_delete_confirm),
            onCancel = close,
            onConfirm = { onEvent(SlotsEvent.ConfirmDelete) },
        )
    }
}
