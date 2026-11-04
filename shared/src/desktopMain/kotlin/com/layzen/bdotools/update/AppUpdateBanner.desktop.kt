package com.layzen.bdotools.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.update_available
import com.layzen.bdotools.resources.update_available_portable
import com.layzen.bdotools.resources.update_download_zip
import com.layzen.bdotools.resources.update_downloading
import com.layzen.bdotools.resources.update_failed
import com.layzen.bdotools.resources.update_install
import com.layzen.bdotools.resources.update_later
import com.layzen.bdotools.resources.update_notes
import com.layzen.bdotools.resources.update_retry
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ProgressBar
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Плашка обновления: версия и кнопки «Что нового», «Обновить» (для портативной копии — «Скачать архив»)
 * и «Позже»; во время скачивания — полоса прогресса, при ошибке — «Повторить».
 */
@Composable
internal actual fun AppUpdateBanner() {
    val viewModel = viewModel { UpdateViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state
    if (current == UpdateState.Hidden) return
    Row(
        Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.backgroundRaised)
            .padding(horizontal = AppTheme.dimens.spaceXl, vertical = AppTheme.dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
    ) {
        when (current) {
            is UpdateState.Available -> AvailableContent(current, viewModel)
            is UpdateState.Downloading -> DownloadingContent(current)
            is UpdateState.Failed -> FailedContent(viewModel)
            UpdateState.Hidden -> Unit
        }
    }
    if (current is UpdateState.Available && current.notesOpen) {
        UpdateNotesDialog(current.info, onClose = { viewModel.showNotes(false) })
    }
}

@Composable
private fun RowScope.AvailableContent(state: UpdateState.Available, viewModel: UpdateViewModel) {
    val uriHandler = LocalUriHandler.current
    val version = state.info.version.toString()
    Text(
        stringResource(if (state.portable) Res.string.update_available_portable else Res.string.update_available, version),
        Modifier.weight(1f),
        style = AppTheme.typography.bodyBold,
        color = AppTheme.colors.accentGold,
    )
    ActionButton(stringResource(Res.string.update_notes), onClick = { viewModel.showNotes(true) })
    ActionButton(stringResource(Res.string.update_later), onClick = viewModel::dismiss)
    if (state.portable) {
        ActionButton(
            stringResource(Res.string.update_download_zip),
            onClick = {
                uriHandler.openUri(state.info.zipUrl)
                viewModel.dismiss()
            },
            enabled = state.info.zipUrl.isNotEmpty(),
        )
    } else {
        ActionButton(stringResource(Res.string.update_install), onClick = viewModel::install)
    }
}

@Composable
private fun RowScope.DownloadingContent(state: UpdateState.Downloading) {
    val percent = state.progress?.let { (it * 100).toInt() }
    Text(
        stringResource(Res.string.update_downloading, state.info.version.toString(), percent ?: 0),
        Modifier.weight(1f),
        style = AppTheme.typography.bodyBold,
        color = AppTheme.colors.textPrimary,
    )
    ProgressBar(state.progress ?: 0f, Modifier.width(UpdateDimens.progressWidth))
}

@Composable
private fun RowScope.FailedContent(viewModel: UpdateViewModel) {
    WarningText(stringResource(Res.string.update_failed), Modifier.weight(1f))
    ActionButton(stringResource(Res.string.update_later), onClick = viewModel::dismiss)
    ActionButton(stringResource(Res.string.update_retry), onClick = viewModel::install)
}
