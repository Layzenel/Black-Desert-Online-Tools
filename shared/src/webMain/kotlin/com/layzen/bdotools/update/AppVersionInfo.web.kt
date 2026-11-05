package com.layzen.bdotools.update

import androidx.compose.runtime.Composable

/** Версия открытой страницы. О новой версии сайта говорит плашка [AppUpdateBanner]. */
@Composable
internal actual fun AppVersionInfo() = VersionPanel { VersionText(WEB_VERSION) }
