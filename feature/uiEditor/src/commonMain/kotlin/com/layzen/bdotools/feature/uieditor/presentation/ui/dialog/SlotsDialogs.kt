package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.preview.LayoutPreviewDialog
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_delete_confirm
import com.layzen.bdotools.feature.uieditor.resources.slots_delete_description
import com.layzen.bdotools.feature.uieditor.resources.slots_delete_title
import org.jetbrains.compose.resources.stringResource

/** Открытое окно страницы, если оно есть. */
@Composable
internal fun SlotsDialogs(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    when (val dialog = state.dialog) {
        null -> Unit
        is SlotsDialog.SaveAs -> SaveAsDialog(dialog, onEvent)
        is SlotsDialog.Import -> ImportDialog(dialog, onEvent)
        is SlotsDialog.Preview -> state.loaded?.game?.screen?.let {
            LayoutPreviewDialog(dialog, it, state.background, state.backgroundShown, state.backgroundDim, onEvent)
        }
        is SlotsDialog.ConfirmApply -> ApplyDialog(dialog, state.loaded?.presets?.keys?.sorted().orEmpty(), onEvent)
        is SlotsDialog.ChatSize -> {
            val chat = state.layout.chats[dialog.window]
            val screen = state.loaded?.game?.screen
            if (chat != null && screen != null) ChatSizeDialog(dialog, chat, screen, onEvent)
        }
        is SlotsDialog.ConfirmDelete -> ConfirmDialog(
            title = stringResource(Res.string.slots_delete_title),
            text = stringResource(Res.string.slots_delete_description, dialog.name),
            confirm = stringResource(Res.string.slots_delete_confirm),
            onCancel = close,
            onConfirm = { onEvent(SlotsEvent.ConfirmDelete) },
        )
    }
}
