package com.layzen.bdotools.feature.portraits.presentation.ui.restore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitItem
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraits.presentation.state.RestoreDialogState
import com.layzen.bdotools.feature.portraits.presentation.ui.PortraitsDimens
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_apply
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_apply_count
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_empty
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_loading
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_title_all
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_title_one
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.ModalWindow
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Восстановление из `FaceTexture_backup`.
 * Для всех портретов: слева список копий, справа её портреты; отмеченные заменят текущие.
 * Для одного портрета: текущий портрет и его версии из всех копий, где он есть.
 */
@Composable
internal fun RestoreDialog(state: RestoreDialogState, portraits: List<PortraitItem>, onEvent: (PortraitsEvent) -> Unit) {
    val single = state.target as? ReplaceTarget.One
    val restoreCount = if (single != null) 1 else state.checked.size
    ModalWindow(
        title = stringResource(if (single != null) Res.string.portraits_restore_title_one else Res.string.portraits_restore_title_all),
        onDismissRequest = { onEvent(PortraitsEvent.CloseRestore) },
        modifier = Modifier.width(if (single != null) PortraitsDimens.restoreOneDialogWidth else PortraitsDimens.restoreDialogWidth),
        footer = {
            if (state.isLoadingPreviews) LoadingRow(stringResource(Res.string.portraits_restore_loading), Modifier.weight(1f))
            ActionButton(stringResource(Res.string.portraits_cancel), onClick = { onEvent(PortraitsEvent.CloseRestore) })
            ActionButton(
                if (single != null) {
                    stringResource(Res.string.portraits_restore_apply)
                } else {
                    stringResource(Res.string.portraits_restore_apply_count, restoreCount)
                },
                onClick = { onEvent(PortraitsEvent.ApplyRestore) },
                enabled = state.selected != null && restoreCount > 0,
            )
        },
    ) {
        Box(Modifier.padding(AppTheme.dimens.spaceL)) {
            when {
                state.backups.isEmpty() -> DescriptionText(stringResource(Res.string.portraits_restore_empty))
                single != null -> SingleRestore(state, portraits.firstOrNull { it.characterId == single.characterId }, single.characterId, onEvent)
                else -> AllRestore(state, onEvent)
            }
        }
    }

    if (state.confirmDelete) DeleteBackupsConfirmation(state, onEvent)
}
