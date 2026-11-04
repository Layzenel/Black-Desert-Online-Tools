package com.layzen.bdotools.feature.portraits.presentation.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraits.presentation.state.ReorderState
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_favorites_button
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_change
import com.layzen.bdotools.feature.portraits.resources.portraits_reload
import com.layzen.bdotools.feature.portraits.resources.portraits_reorder_button
import com.layzen.bdotools.feature.portraits.resources.portraits_reorder_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_button
import com.layzen.bdotools.feature.portraits.resources.portraits_upload
import com.layzen.bdotools.uikit.components.ActionButton
import org.jetbrains.compose.resources.stringResource

/** Кнопки под сеткой портретов. Пока портреты грузятся, все кнопки неактивны. */
@Composable
internal fun RowScope.PortraitsFooter(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val idle = state.loading == null
    val hasPortraits = idle && state.portraits.isNotEmpty()
    ActionButton(stringResource(Res.string.portraits_folder_change), onClick = { onEvent(PortraitsEvent.SelectFolder) }, enabled = idle)
    ActionButton(stringResource(Res.string.portraits_reload), onClick = { onEvent(PortraitsEvent.Reload) }, enabled = idle)
    if (state.reorder == null) {
        ActionButton(
            stringResource(Res.string.portraits_reorder_button),
            onClick = { onEvent(PortraitsEvent.StartReorder) },
            enabled = idle && state.portraits.size >= ReorderState.SWAP_SIZE,
        )
    } else {
        ActionButton(stringResource(Res.string.portraits_reorder_cancel), onClick = { onEvent(PortraitsEvent.CancelReorder) })
    }
    ActionButton(
        stringResource(Res.string.portraits_restore_button),
        onClick = { onEvent(PortraitsEvent.OpenRestore(ReplaceTarget.All)) },
        enabled = idle && state.backups.isNotEmpty(),
    )
    ActionButton(
        stringResource(Res.string.portraits_favorites_button),
        onClick = { onEvent(PortraitsEvent.OpenFavorites) },
        enabled = hasPortraits,
    )
    ActionButton(
        stringResource(Res.string.portraits_upload),
        onClick = { onEvent(PortraitsEvent.OpenSource(ReplaceTarget.All)) },
        enabled = hasPortraits,
    )
}
