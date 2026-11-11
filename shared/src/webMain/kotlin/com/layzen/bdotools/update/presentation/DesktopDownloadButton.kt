package com.layzen.bdotools.update.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.layzen.bdotools.core.app.DESKTOP_INSTALLER_URL
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.desktop_download
import com.layzen.bdotools.resources.menu_download
import com.layzen.bdotools.uikit.components.ToolbarButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Иконка установщика настольной версии; чем она лучше сайта — в подсказке при наведении. */
@Composable
internal fun DesktopDownloadButton() {
    val uriHandler = LocalUriHandler.current
    ToolbarButton(
        painterResource(Res.drawable.menu_download),
        stringResource(Res.string.desktop_download),
        onClick = { uriHandler.openUri(DESKTOP_INSTALLER_URL) },
    )
}
