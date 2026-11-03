package com.layzen.bdotools.feature.portraits.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.feature.portraits.data.FolderAccess
import com.layzen.bdotools.feature.portraits.domain.PortraitTexture
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_checking_access
import com.layzen.bdotools.feature.portraits.resources.portraits_dismiss
import com.layzen.bdotools.feature.portraits.resources.portraits_empty
import com.layzen.bdotools.feature.portraits.resources.portraits_error_blocked
import com.layzen.bdotools.feature.portraits.resources.portraits_error_network
import com.layzen.bdotools.feature.portraits.resources.portraits_error_not_game_folder
import com.layzen.bdotools.feature.portraits.resources.portraits_error_not_image
import com.layzen.bdotools.feature.portraits.resources.portraits_error_permission
import com.layzen.bdotools.feature.portraits.resources.portraits_error_unknown
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_change
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_description
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_select
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_title
import com.layzen.bdotools.feature.portraits.resources.portraits_grid_description
import com.layzen.bdotools.feature.portraits.resources.portraits_grid_title
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_badges
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_caption
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_replace
import com.layzen.bdotools.feature.portraits.resources.portraits_menu_restore
import com.layzen.bdotools.feature.portraits.resources.portraits_restore_button
import com.layzen.bdotools.feature.portraits.resources.portraits_restored
import com.layzen.bdotools.feature.portraits.resources.portraits_backups_deleted
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_deleting
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_loading
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_progress
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_rendering
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_restoring
import com.layzen.bdotools.feature.portraits.resources.portraits_busy_saving
import com.layzen.bdotools.feature.portraits.resources.portraits_permission_description
import com.layzen.bdotools.feature.portraits.resources.portraits_permission_request
import com.layzen.bdotools.feature.portraits.resources.portraits_reload
import com.layzen.bdotools.feature.portraits.resources.portraits_decoration_saved
import com.layzen.bdotools.feature.portraits.resources.portraits_saved
import com.layzen.bdotools.feature.portraits.resources.portraits_title
import com.layzen.bdotools.feature.portraits.resources.portraits_unsupported_description
import com.layzen.bdotools.feature.portraits.resources.portraits_unsupported_title
import com.layzen.bdotools.feature.portraits.resources.portraits_upload
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.BusyOverlay
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.HoverMenuBox
import com.layzen.bdotools.uikit.components.PopupMenuDivider
import com.layzen.bdotools.uikit.components.PopupMenuItem
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Tile
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortraitsRoute(viewModel: PortraitsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PortraitsScreen(state, viewModel::onEvent)
}

@Composable
fun PortraitsScreen(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val ready = state.access is FolderAccess.Granted
    val decorator = rememberPortraitDecorator()
    LaunchedEffect(decorator, decorator.isReady) { onEvent(PortraitsEvent.DecoratorChanged(decorator)) }
    GameWindow(
        title = stringResource(Res.string.portraits_title),
        modifier = Modifier.widthIn(max = PortraitsDimens.windowMaxWidth),
        footer = if (ready) {
            {
                ActionButton(stringResource(Res.string.portraits_folder_change), onClick = { onEvent(PortraitsEvent.SelectFolder) })
                ActionButton(stringResource(Res.string.portraits_reload), onClick = { onEvent(PortraitsEvent.Reload) })
                ActionButton(
                    stringResource(Res.string.portraits_restore_button),
                    onClick = { onEvent(PortraitsEvent.OpenRestore(ReplaceTarget.All)) },
                    enabled = state.backups.isNotEmpty(),
                )
                ActionButton(
                    stringResource(Res.string.portraits_upload),
                    onClick = { onEvent(PortraitsEvent.OpenSource(ReplaceTarget.All)) },
                    enabled = state.portraits.isNotEmpty(),
                )
            }
        } else {
            null
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            state.notice?.let { NoticeBar(it, onDismiss = { onEvent(PortraitsEvent.DismissNotice) }) }
            when (val access = state.access) {
                null -> DescriptionText(stringResource(Res.string.portraits_checking_access))
                FolderAccess.Unsupported -> SectionHeader(
                    stringResource(Res.string.portraits_unsupported_title),
                    warning = stringResource(Res.string.portraits_unsupported_description),
                )
                FolderAccess.NotSelected -> FolderPrompt(
                    description = stringResource(Res.string.portraits_folder_description),
                    button = stringResource(Res.string.portraits_folder_select),
                    onClick = { onEvent(PortraitsEvent.SelectFolder) },
                )
                is FolderAccess.NeedsPermission -> FolderPrompt(
                    description = stringResource(Res.string.portraits_permission_description, access.folderName),
                    button = stringResource(Res.string.portraits_permission_request),
                    onClick = { onEvent(PortraitsEvent.RequestPermission) },
                )
                is FolderAccess.Granted -> PortraitsContent(state, onEvent)
            }
        }
    }

    state.sourceDialog?.let { SourceDialog(it, onEvent) }
    state.editor?.let { MaskEditor(it, onEvent) }
    state.decorationDialog?.let { DecorationDialog(it, decorator, onEvent) }
    state.restoreDialog?.let { RestoreDialog(it, state.portraits, onEvent) }
    state.busy?.let { BusyIndicator(it) }
}

@Composable
private fun BusyIndicator(busy: Busy) {
    val text = when (busy.kind) {
        BusyKind.Loading -> Res.string.portraits_busy_loading
        BusyKind.Rendering -> Res.string.portraits_busy_rendering
        BusyKind.Saving -> Res.string.portraits_busy_saving
        BusyKind.Restoring -> Res.string.portraits_busy_restoring
        BusyKind.Deleting -> Res.string.portraits_busy_deleting
    }
    BusyOverlay(
        text = stringResource(text),
        progress = busy.progress,
        details = if (busy.total > 0) stringResource(Res.string.portraits_busy_progress, busy.done, busy.total) else null,
    )
}

@Composable
private fun FolderPrompt(description: String, button: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL)) {
        SectionHeader(stringResource(Res.string.portraits_folder_title), Modifier.weight(1f), description = description)
        ActionButton(button, onClick = onClick)
    }
}

@Composable
private fun PortraitsContent(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    when {
        state.busy?.kind == BusyKind.Loading && state.portraits.isEmpty() -> Unit
        state.portraits.isEmpty() -> DescriptionText(stringResource(Res.string.portraits_empty))
        else -> {
            SectionHeader(
                stringResource(Res.string.portraits_grid_title, state.portraits.size),
                description = stringResource(Res.string.portraits_grid_description),
            )
            PortraitGridView(state, onEvent)
        }
    }
}

/**
 * Сетка как в окне выбора персонажа: до 7 портретов в ряду, неполный ряд прижат влево.
 * Клик по портрету открывает его замену. При наведении появляется меню: заменить портрет, надпись, значки
 * и, если у портрета есть резервные копии, восстановление из них.
 */
@Composable
private fun PortraitGridView(state: PortraitsState, onEvent: (PortraitsEvent) -> Unit) {
    val portraits = state.portraits
    val columns = minOf(portraits.size, PortraitTexture.MAX_COLUMNS)
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        portraits.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
                row.forEach { portrait ->
                    val id = portrait.characterId
                    HoverMenuBox(
                        modifier = Modifier.weight(1f).aspectRatio(PortraitTexture.WIDTH.toFloat() / PortraitTexture.HEIGHT),
                        onClick = { onEvent(PortraitsEvent.OpenSource(ReplaceTarget.One(id))) },
                        menu = { close ->
                            PopupMenuItem(stringResource(Res.string.portraits_menu_replace), onClick = {
                                close()
                                onEvent(PortraitsEvent.OpenSource(ReplaceTarget.One(id)))
                            })
                            PopupMenuDivider()
                            PopupMenuItem(stringResource(Res.string.portraits_menu_caption), onClick = {
                                close()
                                onEvent(PortraitsEvent.OpenDecoration(id, DecorationMode.Caption))
                            })
                            PopupMenuDivider()
                            PopupMenuItem(stringResource(Res.string.portraits_menu_badges), onClick = {
                                close()
                                onEvent(PortraitsEvent.OpenDecoration(id, DecorationMode.Badges))
                            })
                            if (state.hasBackups(id)) {
                                PopupMenuDivider()
                                PopupMenuItem(stringResource(Res.string.portraits_menu_restore), onClick = {
                                    close()
                                    onEvent(PortraitsEvent.OpenRestore(ReplaceTarget.One(id)))
                                })
                            }
                        },
                    ) { menuOpen ->
                        Tile(modifier = Modifier.matchParentSize(), selected = menuOpen) {
                            Image(
                                bitmap = portrait.image,
                                contentDescription = id,
                                contentScale = ContentScale.Crop,
                                filterQuality = FilterQuality.Medium,
                                modifier = Modifier.matchParentSize(),
                            )
                        }
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun NoticeBar(notice: Notice, onDismiss: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM)) {
        val text = noticeText(notice)
        if (notice.isError) {
            WarningText(text, Modifier.weight(1f))
        } else {
            Text(text, Modifier.weight(1f), style = AppTheme.typography.bodyBold, color = AppTheme.colors.accentGold)
        }
        ActionButton(stringResource(Res.string.portraits_dismiss), onClick = onDismiss)
    }
}

@Composable
internal fun noticeText(notice: Notice): String = when (notice.kind) {
    NoticeKind.Saved -> stringResource(Res.string.portraits_saved, notice.argument)
    NoticeKind.DecorationSaved -> stringResource(Res.string.portraits_decoration_saved, notice.argument)
    NoticeKind.Restored -> stringResource(Res.string.portraits_restored, notice.argument)
    NoticeKind.BackupsDeleted -> stringResource(Res.string.portraits_backups_deleted, notice.argument.toInt())
    NoticeKind.NotGameFolder -> stringResource(Res.string.portraits_error_not_game_folder)
    NoticeKind.Permission -> stringResource(Res.string.portraits_error_permission)
    NoticeKind.Blocked -> stringResource(Res.string.portraits_error_blocked)
    NoticeKind.Network -> stringResource(Res.string.portraits_error_network)
    NoticeKind.NotImage -> stringResource(Res.string.portraits_error_not_image)
    NoticeKind.Unknown -> stringResource(Res.string.portraits_error_unknown, notice.argument)
}
