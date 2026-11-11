package com.layzen.bdotools.update.presentation

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

/** Версия открытой страницы и ссылка на настольную версию. О новой версии сайта говорит плашка [AppUpdateBanner]. */
@Composable
internal actual fun AppVersionInfo() = VersionPanel {
    VersionText(koinViewModel<WebUpdateViewModel>().ownVersion)
    DesktopDownloadButton()
}
