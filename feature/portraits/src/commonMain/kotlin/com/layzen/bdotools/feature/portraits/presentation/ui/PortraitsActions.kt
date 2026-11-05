package com.layzen.bdotools.feature.portraits.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraits.presentation.state.ReorderState
import com.layzen.bdotools.feature.portraits.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.menu_backups
import com.layzen.bdotools.feature.portraits.resources.menu_favorites
import com.layzen.bdotools.feature.portraits.resources.menu_folder
import com.layzen.bdotools.feature.portraits.resources.menu_reload
import com.layzen.bdotools.feature.portraits.resources.menu_reorder
import com.layzen.bdotools.feature.portraits.resources.menu_upload
import com.layzen.bdotools.feature.portraits.resources.portraits_favorites_button
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_change
import com.layzen.bdotools.feature.portraits.resources.portraits_reload
import com.layzen.bdotools.feature.portraits.resources.portraits_reorder_button
import com.layzen.bdotools.feature.portraits.resources.portraits_reorder_cancel
import com.layzen.bdotools.feature.portraits.resources.portraits_reorder_hotkey
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_button
import com.layzen.bdotools.feature.portraits.resources.portraits_upload
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Строка действий под сеткой портретов: папка игры слева, главное «Загрузить» — последним справа.
 * Пока портреты грузятся, действия неактивны. В режиме изменения порядка его пункт подсвечен и выключает режим.
 */
@Composable
internal fun PortraitsActions(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val idle = state.loading == null
    val hasPortraits = idle && state.portraits.isNotEmpty()
    val reordering = state.reorder != null
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        val item = Modifier.weight(1f)
        MenuAction(
            stringResource(Res.string.portraits_folder_change),
            onClick = { onEvent(PortraitsEvent.SelectFolder) },
            modifier = item,
            icon = painterResource(Res.drawable.menu_folder),
            enabled = idle,
        )
        MenuAction(
            stringResource(Res.string.portraits_reload),
            onClick = { onEvent(PortraitsEvent.Reload) },
            modifier = item,
            icon = painterResource(Res.drawable.menu_reload),
            enabled = idle,
        )
        MenuAction(
            stringResource(if (reordering) Res.string.portraits_reorder_cancel else Res.string.portraits_reorder_button),
            onClick = { onEvent(if (reordering) PortraitsEvent.CancelReorder else PortraitsEvent.StartReorder) },
            modifier = item,
            icon = painterResource(Res.drawable.menu_reorder),
            hotkey = if (reordering) stringResource(Res.string.portraits_reorder_hotkey) else null,
            selected = reordering,
            enabled = reordering || idle && state.portraits.size >= ReorderState.SWAP_SIZE,
        )
        MenuAction(
            stringResource(Res.string.portraits_restore_button),
            onClick = { onEvent(PortraitsEvent.OpenRestore(ReplaceTarget.All)) },
            modifier = item,
            icon = painterResource(Res.drawable.menu_backups),
            enabled = idle && state.backups.isNotEmpty(),
        )
        MenuAction(
            stringResource(Res.string.portraits_favorites_button),
            onClick = { onEvent(PortraitsEvent.OpenFavorites) },
            modifier = item,
            icon = painterResource(Res.drawable.menu_favorites),
            enabled = hasPortraits,
        )
        MenuAction(
            stringResource(Res.string.portraits_upload),
            onClick = { onEvent(PortraitsEvent.OpenSource(ReplaceTarget.All)) },
            modifier = item,
            icon = painterResource(Res.drawable.menu_upload),
            enabled = hasPortraits,
        )
    }
}
