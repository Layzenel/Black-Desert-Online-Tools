package com.layzen.bdotools.update

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.update_downloading
import com.layzen.bdotools.resources.update_failed
import com.layzen.bdotools.resources.update_later
import com.layzen.bdotools.resources.update_retry
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.components.ProgressBar
import com.layzen.bdotools.uikit.components.WarningText
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Плашка обновления: во время скачивания — полоса прогресса, при ошибке — «Позже» и «Повторить». */
@Composable
internal actual fun AppUpdateBanner() {
    val viewModel = viewModel { UpdateViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        is UpdateState.Downloading -> UpdateBannerRow { DownloadingContent(current) }
        is UpdateState.Failed -> UpdateBannerRow { FailedContent(viewModel) }
        UpdateState.Hidden, is UpdateState.Available -> Unit
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
    ActionButton(stringResource(Res.string.update_later), onClick = viewModel::postpone)
    ActionButton(stringResource(Res.string.update_retry), onClick = viewModel::install)
}
