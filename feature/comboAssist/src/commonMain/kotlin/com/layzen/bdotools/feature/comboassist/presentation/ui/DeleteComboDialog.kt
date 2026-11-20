package com.layzen.bdotools.feature.comboassist.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_cancel_selection
import com.layzen.bdotools.feature.comboassist.resources.combo_delete
import com.layzen.bdotools.feature.comboassist.resources.combo_delete_confirm
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Подтверждение удаления комбо [name]: «Удалить комбо» — [onConfirm], «Отмена», крестик и Esc — [onCancel]. */
@Composable
internal fun DeleteComboDialog(name: String, onCancel: () -> Unit, onConfirm: () -> Unit) {
    ModalWindow(
        title = stringResource(Res.string.combo_delete),
        onDismissRequest = onCancel,
        modifier = Modifier.width(ComboDimens.confirmWidth),
        footer = {
            ActionButton(stringResource(Res.string.combo_cancel_selection), onClick = onCancel)
            ActionButton(stringResource(Res.string.combo_delete), onClick = onConfirm)
        },
    ) {
        WarningText(stringResource(Res.string.combo_delete_confirm, name), Modifier.padding(AppTheme.dimens.spaceL))
    }
}
