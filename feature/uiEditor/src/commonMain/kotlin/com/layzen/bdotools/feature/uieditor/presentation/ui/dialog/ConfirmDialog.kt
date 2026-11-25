package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.presentation.ui.editor.title
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel_tooltip
import com.layzen.bdotools.feature.uieditor.resources.slots_confirm_tooltip
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ActionButtonState
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Подтверждение действия: предупреждение, «Отмена» и [confirm] справа. */
@Composable
internal fun ConfirmDialog(title: String, text: String, confirm: String, onCancel: () -> Unit, onConfirm: () -> Unit) {
    ModalWindow(
        title = title,
        onDismissRequest = onCancel,
        modifier = Modifier.width(SlotsDimens.dialogWidth),
        footer = {
            ActionButton(ActionButtonState.Neutral(stringResource(Res.string.slots_cancel), onClick = onCancel, tooltip = stringResource(Res.string.slots_cancel_tooltip)))
            ActionButton(ActionButtonState.Neutral(confirm, onClick = onConfirm, tooltip = stringResource(Res.string.slots_confirm_tooltip)))
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL)) { WarningText(text) }
    }
}
