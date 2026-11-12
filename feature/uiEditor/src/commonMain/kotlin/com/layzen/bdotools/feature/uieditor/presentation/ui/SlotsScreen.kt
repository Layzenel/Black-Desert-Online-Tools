package com.layzen.bdotools.feature.uieditor.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.presentation.SlotsViewModel
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import com.layzen.bdotools.feature.uieditor.presentation.ui.dialog.SlotsDialogs
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.slots_busy
import com.layzen.bdotools.feature.uieditor.resources.slots_checking_access
import com.layzen.bdotools.feature.uieditor.resources.slots_folder_description
import com.layzen.bdotools.feature.uieditor.resources.slots_folder_select
import com.layzen.bdotools.feature.uieditor.resources.slots_loading
import com.layzen.bdotools.feature.uieditor.resources.slots_permission_description
import com.layzen.bdotools.feature.uieditor.resources.slots_permission_request
import com.layzen.bdotools.feature.uieditor.resources.slots_unsupported_description
import com.layzen.bdotools.feature.uieditor.resources.slots_unsupported_title
import com.layzen.bdotools.uikit.components.BusyOverlay
import com.layzen.bdotools.uikit.components.DescriptionText
import com.layzen.bdotools.uikit.components.LoadingRow
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.Toast
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Точка входа экрана: берёт [SlotsViewModel] и передаёт [SlotsScreen] его состояние и события. */
@Composable
fun SlotsRoute(viewModel: SlotsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // ViewModel переживает уход со страницы, а портреты и подписи могли поменять в «Конструкторе портретов»
    LaunchedEffect(viewModel) { viewModel.onEvent(SlotsEvent.Shown) }
    SlotsScreen(state, viewModel::onEvent)
}

/** Экран редактора; пока игра не загружена, показывает выбор папки и доступ к ней. */
@Composable
fun SlotsScreen(state: SlotsState, onEvent: (SlotsEvent) -> Unit) {
    val ready = state.loaded != null
    if (ready) {
        SlotsContent(state, onEvent, Modifier.fillMaxSize())
    } else {
        Column(Modifier.widthIn(max = AppTheme.dimens.contentMaxWidth).padding(AppTheme.dimens.spaceXl)) { AccessContent(state, onEvent) }
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
