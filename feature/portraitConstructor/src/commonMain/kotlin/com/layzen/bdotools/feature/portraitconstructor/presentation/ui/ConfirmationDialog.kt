package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_cancel
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Подтверждение необратимого действия (удаление, перезапись): предупреждение и список того, что затронет
 * действие. Кнопка [confirm] выполняет его, «Отмена» и клик мимо окна — нет.
 */
@Composable
internal fun ConfirmationDialog(
    title: String,
    warning: String,
    items: List<String>,
    confirm: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    ModalWindow(
        title = title,
        onDismissRequest = onCancel,
        modifier = Modifier.width(PortraitsDimens.confirmDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = onCancel)
            ActionButton(confirm, onClick = onConfirm)
        },
    ) {
        Column(
            Modifier
                .padding(AppTheme.dimens.spaceL)
                .heightIn(max = PortraitsDimens.confirmListMaxHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            WarningText(warning)
            items.forEach { Text(it, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige) }
        }
    }
}
