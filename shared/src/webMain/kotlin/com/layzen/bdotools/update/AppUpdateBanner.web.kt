package com.layzen.bdotools.update

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.update_later
import com.layzen.bdotools.resources.web_update_available
import com.layzen.bdotools.resources.web_update_reload
import com.layzen.bdotools.uikit.components.ActionButton
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Плашка «вышла новая версия сайта» с кнопками «Позже» и «Обновить страницу». */
@Composable
internal actual fun AppUpdateBanner() {
    val viewModel = viewModel { WebUpdateViewModel() }
    val newVersion by viewModel.newVersion.collectAsStateWithLifecycle()
    val version = newVersion ?: return
    UpdateBannerRow {
        Text(
            stringResource(Res.string.web_update_available, version),
            Modifier.weight(1f),
            style = AppTheme.typography.bodyBold,
            color = AppTheme.colors.accentGold,
        )
        ActionButton(stringResource(Res.string.update_later), onClick = viewModel::postpone)
        ActionButton(stringResource(Res.string.web_update_reload), onClick = ::reloadPage)
    }
}
