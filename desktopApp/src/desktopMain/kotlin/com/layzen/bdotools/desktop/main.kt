package com.layzen.bdotools.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import com.layzen.bdotools.desktop.resources.Res
import com.layzen.bdotools.desktop.resources.app_icon
import com.layzen.bdotools.desktop.resources.desktop_window_title
import com.layzen.bdotools.window.DesktopApp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Настольная версия: окно на весь экран с тем же приложением, что и на сайте, значок в трее. */
fun main() = application {
    remember { AltMenuBlocker.install() }
    DesktopApp(stringResource(Res.string.desktop_window_title), painterResource(Res.drawable.app_icon))
}
