package com.layzen.bdotools.feature.portraits.presentation.ui.restore

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
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.RestoreDialogState
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_delete_confirm
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_delete_text
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_delete_title
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Подтверждение удаления отмеченных копий: перечисляет их, потому что удаление необратимо. */
@Composable
internal fun DeleteBackupsConfirmation(state: RestoreDialogState, onEvent: (PortraitsEvent) -> Unit) {
    val marked = state.backups.filter { it.name in state.marked }
    ModalWindow(
        title = stringResource(Res.string.portraits_backups_delete_title),
        onDismissRequest = { onEvent(PortraitsEvent.CancelDeleteBackups) },
        modifier = Modifier.width(PortraitsDimens.confirmDialogWidth),
        footer = {
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CancelDeleteBackups) })
            ActionButton(stringResource(Res.string.portraits_backups_delete_confirm), onClick = { onEvent(PortraitsEvent.ConfirmDeleteBackups) })
        },
    ) {
        Column(
            Modifier
                .padding(AppTheme.dimens.spaceL)
                .heightIn(max = PortraitsDimens.confirmListMaxHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        ) {
            WarningText(stringResource(Res.string.portraits_backups_delete_text, marked.size))
            marked.forEach { backup ->
                Text(backup.label, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
            }
        }
    }
}
