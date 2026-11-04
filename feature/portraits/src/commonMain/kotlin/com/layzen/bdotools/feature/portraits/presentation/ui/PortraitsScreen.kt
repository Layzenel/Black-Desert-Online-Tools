package com.layzen.bdotools.feature.portraits.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.portraits.presentation.PortraitsViewModel
import com.layzen.bdotools.feature.portraits.presentation.event.PortraitsEvent
import com.layzen.bdotools.feature.portraits.presentation.render.rememberPortraitDecorator
import com.layzen.bdotools.feature.portraits.presentation.state.PortraitsState
import com.layzen.bdotools.feature.portraits.presentation.ui.decoration.DecorationDialog
import com.layzen.bdotools.feature.portraits.presentation.ui.editor.MaskEditor
import com.layzen.bdotools.feature.portraits.presentation.ui.favorites.FavoritesDialog
import com.layzen.bdotools.feature.portraits.presentation.ui.notice.noticeText
import com.layzen.bdotools.feature.portraits.presentation.ui.restore.RestoreDialog
import com.layzen.bdotools.feature.portraits.presentation.ui.source.SourceDialog
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.portraits_checking_access
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_description
import com.layzen.bdotools.feature.portraits.resources.portraits_folder_select
import com.layzen.bdotools.feature.portraits.resources.portraits_permission_description
import com.layzen.bdotools.feature.portraits.resources.portraits_permission_request
import com.layzen.bdotools.feature.portraits.resources.portraits_title
import com.layzen.bdotools.feature.portraits.resources.portraits_unsupported_description
import com.layzen.bdotools.feature.portraits.resources.portraits_unsupported_title
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Toast
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
            { PortraitsFooter(state, onEvent) }
        } else {
            null
        },
    ) {
        Column(
            Modifier.padding(AppTheme.dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
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
    state.favoritesDialog?.let { FavoritesDialog(it, onEvent) }
    state.busy?.let { BusyIndicator(it) }
    val notice = state.notice
    Toast(
        text = notice?.let { noticeText(it) },
        onDismiss = { onEvent(PortraitsEvent.DismissNotice) },
        isError = notice?.isError == true,
    )
}
