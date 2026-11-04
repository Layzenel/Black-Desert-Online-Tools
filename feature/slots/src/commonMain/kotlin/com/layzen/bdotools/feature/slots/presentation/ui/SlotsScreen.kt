package com.layzen.bdotools.feature.slots.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.feature.slots.data.FolderAccess
import com.layzen.bdotools.feature.slots.presentation.SlotsViewModel
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.dialog.SlotsDialogs
import com.layzen.bdotools.feature.slots.resources.Res
import com.layzen.bdotools.feature.slots.resources.slots_busy
import com.layzen.bdotools.feature.slots.resources.slots_checking_access
import com.layzen.bdotools.feature.slots.resources.slots_folder_description
import com.layzen.bdotools.feature.slots.resources.slots_folder_select
import com.layzen.bdotools.feature.slots.resources.slots_loading
import com.layzen.bdotools.feature.slots.resources.slots_permission_description
import com.layzen.bdotools.feature.slots.resources.slots_permission_request
import com.layzen.bdotools.feature.slots.resources.slots_title
import com.layzen.bdotools.feature.slots.resources.slots_unsupported_description
import com.layzen.bdotools.feature.slots.resources.slots_unsupported_title
import com.layzen.bdotools.uikit.components.BusyOverlay
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.GameWindow
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Toast
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SlotsRoute(viewModel: SlotsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SlotsScreen(state, viewModel::onEvent)
}

@Composable
fun SlotsScreen(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val ready = state.loaded != null
    GameWindow(
        title = stringResource(Res.string.slots_title),
        modifier = Modifier.widthIn(max = SlotsDimens.windowMaxWidth),
        footer = if (ready) {
            { SlotsFooter(state, onEvent) }
        } else {
            null
        },
    ) {
        if (ready) {
            SlotsContent(state, onEvent)
        } else {
            Column(Modifier.padding(AppTheme.dimens.spaceL)) { AccessContent(state, onEvent) }
        }
    }

    SlotsDialogs(state, onEvent)
    if (state.busy && ready) BusyOverlay(stringResource(Res.string.slots_busy))
    val notice = state.notice
    Toast(
        text = notice?.let { noticeText(it) },
        onDismiss = { onEvent(SlotsEvent.DismissNotice) },
        isError = notice?.isError == true,
    )
}

/** Всё, что показывается до того, как файл игры прочитан: проверка доступа и просьба выбрать папку. */
@Composable
private fun AccessContent(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    when (val access = state.access) {
        null -> DescriptionText(stringResource(Res.string.slots_checking_access))
        FolderAccess.Unsupported -> SectionHeader(
            stringResource(Res.string.slots_unsupported_title),
            warning = stringResource(Res.string.slots_unsupported_description),
        )
        FolderAccess.NotSelected -> FolderPrompt(
            description = stringResource(Res.string.slots_folder_description),
            button = stringResource(Res.string.slots_folder_select),
            onClick = { onEvent(SlotsEvent.SelectFolder) },
        )
        is FolderAccess.NeedsPermission -> FolderPrompt(
            description = stringResource(Res.string.slots_permission_description, access.folderName),
            button = stringResource(Res.string.slots_permission_request),
            onClick = { onEvent(SlotsEvent.RequestPermission) },
        )
        is FolderAccess.Granted -> if (state.busy) {
            LoadingRow(stringResource(Res.string.slots_loading))
        } else {
            FolderPrompt(
                description = stringResource(Res.string.slots_folder_description),
                button = stringResource(Res.string.slots_folder_select),
                onClick = { onEvent(SlotsEvent.SelectFolder) },
            )
        }
    }
}
