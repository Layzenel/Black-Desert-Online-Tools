package com.layzen.bdotools.feature.slots.presentation.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsDialog
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_cancel
import com.layzen.bdotools.feature.slots.resources.slots_save
import com.layzen.bdotools.feature.slots.resources.slots_save_as_description
import com.layzen.bdotools.feature.slots.resources.slots_save_as_name
import com.layzen.bdotools.feature.slots.resources.slots_save_as_title
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.InputField
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Название нового пресета. */
@Composable
internal fun SaveAsDialog(dialog: SlotsDialog.SaveAs, onEvent: (SlotsEvent) -> Unit) {
    val close = { onEvent(SlotsEvent.CloseDialog) }
    ModalWindow(
        title = stringResource(Res.string.slots_save_as_title),
        onDismissRequest = close,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.slots_cancel), onClick = close)
            ActionButton(
                stringResource(Res.string.slots_save),
                onClick = { onEvent(SlotsEvent.ConfirmSaveAs) },
                enabled = dialog.name.isNotBlank(),
            )
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL), verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            InputField(
                value = dialog.name,
                onValueChange = { onEvent(SlotsEvent.SaveAsNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.slots_save_as_name),
            )
            DescriptionText(stringResource(Res.string.slots_save_as_description))
        }
    }
}
