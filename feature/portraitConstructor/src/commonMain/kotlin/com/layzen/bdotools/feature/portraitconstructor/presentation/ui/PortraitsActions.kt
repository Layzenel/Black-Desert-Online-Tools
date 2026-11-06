package com.layzen.bdotools.feature.portraitconstructor.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.portraitconstructor.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReorderState
import com.layzen.bdotools.feature.portraitconstructor.presentation.state.ReplaceTarget
import com.layzen.bdotools.feature.portraitconstructor.resources.Res
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_backups
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_favorites
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_folder
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_notes
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_reload
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_reorder
import com.layzen.bdotools.feature.portraitconstructor.resources.menu_upload
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_favorites_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_folder_change
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_folder_change_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_notes_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reload
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reload_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_cancel
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_cancel_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_reorder_hotkey
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_button
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_restore_hint
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_upload
import com.layzen.bdotools.feature.portraitconstructor.resources.portraits_upload_hint
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.MenuAction
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Строка действий под сеткой портретов: папка игры слева, главное «Загрузить» — последним справа.
 * Пока портреты грузятся, действия неактивны. В режиме изменения порядка его пункт подсвечен и выключает режим.
 * Подписи короткие, поэтому у каждого действия при наведении — подсказка, что оно делает.
 */
@Composable
internal fun PortraitsActions(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val idle = state.loading == null
    val hasPortraits = idle && state.portraits.isNotEmpty()
    val reordering = state.reorder != null
    val fill = Modifier.fillMaxWidth()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        val item = Modifier.weight(1f)
        // TooltipBox не передаёт weight в Row, поэтому вес — у обёртки, а подсказка внутри неё
        Box(item) {
            HoverTooltip(stringResource(Res.string.portraits_folder_change_hint)) {
                MenuAction(
                    stringResource(Res.string.portraits_folder_change),
                    onClick = { onEvent(PortraitsEvent.SelectFolder) },
                    modifier = fill,
                    icon = painterResource(Res.drawable.menu_folder),
                    enabled = idle,
                )
            }
        }
        Box(item) {
            HoverTooltip(stringResource(Res.string.portraits_reload_hint)) {
                MenuAction(
                    stringResource(Res.string.portraits_reload),
                    onClick = { onEvent(PortraitsEvent.Reload) },
                    modifier = fill,
                    icon = painterResource(Res.drawable.menu_reload),
                    enabled = idle,
                )
            }
        }
        Box(item) {
            HoverTooltip(stringResource(if (reordering) Res.string.portraits_reorder_cancel_hint else Res.string.portraits_reorder_hint)) {
                MenuAction(
                    stringResource(if (reordering) Res.string.portraits_reorder_cancel else Res.string.portraits_reorder_button),
                    onClick = { onEvent(if (reordering) PortraitsEvent.CancelReorder else PortraitsEvent.StartReorder) },
                    modifier = fill,
                    icon = painterResource(Res.drawable.menu_reorder),
                    hotkey = if (reordering) stringResource(Res.string.portraits_reorder_hotkey) else null,
                    selected = reordering,
                    enabled = reordering || idle && state.portraits.size >= ReorderState.SWAP_SIZE,
                )
            }
        }
        Box(item) {
            HoverTooltip(stringResource(Res.string.portraits_restore_hint)) {
                MenuAction(
                    stringResource(Res.string.portraits_restore_button),
                    onClick = { onEvent(PortraitsEvent.OpenRestore(ReplaceTarget.All)) },
                    modifier = fill,
                    icon = painterResource(Res.drawable.menu_backups),
                    enabled = idle && state.backups.isNotEmpty(),
                )
            }
        }
        Box(item) {
            HoverTooltip(stringResource(Res.string.portraits_favorites_hint)) {
                MenuAction(
                    stringResource(Res.string.portraits_favorites_button),
                    onClick = { onEvent(PortraitsEvent.OpenFavorites) },
                    modifier = fill,
                    icon = painterResource(Res.drawable.menu_favorites),
                    enabled = hasPortraits,
                )
            }
        }
        Box(item) {
            HoverTooltip(stringResource(Res.string.portraits_notes_hint)) {
                MenuAction(
                    stringResource(Res.string.portraits_notes_button),
                    onClick = { onEvent(PortraitsEvent.OpenNotesSync) },
                    modifier = fill,
                    icon = painterResource(Res.drawable.menu_notes),
                    enabled = hasPortraits && state.accountId != null,
                )
            }
        }
        Box(item) {
            HoverTooltip(stringResource(Res.string.portraits_upload_hint)) {
                MenuAction(
                    stringResource(Res.string.portraits_upload),
                    onClick = { onEvent(PortraitsEvent.OpenSource(ReplaceTarget.All)) },
                    modifier = fill,
                    icon = painterResource(Res.drawable.menu_upload),
                    enabled = hasPortraits,
                )
            }
        }
    }
}
