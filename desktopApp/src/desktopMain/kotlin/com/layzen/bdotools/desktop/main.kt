package com.layzen.bdotools.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.App
import com.layzen.bdotools.desktop.resources.Res
import com.layzen.bdotools.desktop.resources.app_icon
import com.layzen.bdotools.desktop.resources.desktop_window_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Настольная версия: одно окно на весь экран с тем же [App], что и на сайте. */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(placement = WindowPlacement.Maximized),
        title = stringResource(Res.string.desktop_window_title),
        icon = painterResource(Res.drawable.app_icon),
    ) {
        App()
    }
}
