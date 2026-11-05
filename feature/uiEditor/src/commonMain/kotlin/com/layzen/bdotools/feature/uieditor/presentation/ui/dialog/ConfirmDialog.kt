package com.layzen.bdotools.feature.uieditor.presentation.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_cancel
import com.layzen.bdotools.uikit.components.ActionButton
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
            ActionButton(stringResource(Res.string.slots_cancel), onClick = onCancel)
            ActionButton(confirm, onClick = onConfirm)
        },
    ) {
        Column(Modifier.padding(AppTheme.dimens.spaceL)) { WarningText(text) }
    }
}
