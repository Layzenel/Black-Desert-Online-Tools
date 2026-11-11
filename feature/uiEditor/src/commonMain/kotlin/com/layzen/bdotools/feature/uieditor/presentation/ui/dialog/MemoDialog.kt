package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_add_title
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_description
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_edit_title
import com.layzen.bdotools.feature.uieditor.resources.slots_memo_placeholder
import com.layzen.bdotools.feature.uieditor.resources.slots_save
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Надпись навыка под слотом перезарядки, как в игре: до [SlotLabel.MAX_CHARS] знаков. Пустая убирает надпись. */
@Composable
internal fun MemoDialog(dialog: SlotsDialog.EditMemo, skillName: String, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    ModalWindow(
        title = stringResource(if (dialog.existing) Res.string.slots_memo_edit_title else Res.string.slots_memo_add_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(stringResource(Res.string.slots_save), onClick = { onEvent(SlotsEvent.ConfirmMemo) })
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            DescriptionText(skillName)
            InputField(
                value = dialog.text,
                onValueChange = { onEvent(SlotsEvent.MemoChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.slots_memo_placeholder),
            )
            DescriptionText(stringResource(Res.string.slots_memo_description, dialog.text.length, SlotLabel.MAX_CHARS))
        }
    }
}
