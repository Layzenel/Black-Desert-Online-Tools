package com.layzen.bdotools.update.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.app_version
import com.layzen.bdotools.resources.app_version_dev
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Номер версии в навигации; `null` — сборка разработчика, у неё номера нет. */
@Composable
internal fun VersionText(version: String?) {
    Text(
        version?.let { stringResource(Res.string.app_version, it) } ?: stringResource(Res.string.app_version_dev),
        style = AppTheme.typography.bodyBold,
        color = AppTheme.colors.textBeige,
        maxLines = 1,
    )
}
