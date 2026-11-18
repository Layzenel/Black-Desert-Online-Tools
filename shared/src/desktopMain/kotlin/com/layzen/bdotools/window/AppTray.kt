package com.layzen.bdotools.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import com.layzen.bdotools.navigation.AppTab
import com.layzen.bdotools.resources.Res
import com.layzen.bdotools.resources.window_close_exit
import org.jetbrains.compose.resources.stringResource
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

/**
 * Значок программы в трее. ЛКМ — показать окно ([onOpen]); ПКМ — меню: вкладки по порядку ([onOpenTab] открывает
 * вкладку и окно) и «Закрыть» ([onExit]). Свой `TrayIcon`, а не `Tray` из Compose: тот открывает окно двойным
 * кликом, а нужно одиночным. Подписи меняются вместе с языком интерфейса.
 */
@Composable
internal fun AppTray(icon: Painter, title: String, onOpen: () -> Unit, onOpenTab: (AppTab) -> Unit, onExit: () -> Unit) {
    if (!SystemTray.isSupported()) return
    val tabs = AppTab.entries.map { it to stringResource(it.title).replace('\n', ' ') }
    val exit = stringResource(Res.string.window_close_exit)
    val density = LocalDensity.current
    val open by rememberUpdatedState(onOpen)
    val openTab by rememberUpdatedState(onOpenTab)
    val close by rememberUpdatedState(onExit)
    DisposableEffect(tabs, exit) {
        val size = WindowDimens.trayIconPx.toFloat()
        val menu = PopupMenu().apply {
            tabs.forEach { (tab, label) -> add(MenuItem(label).apply { addActionListener { SwingUtilities.invokeLater { openTab(tab) } } }) }
            addSeparator()
            add(MenuItem(exit).apply { addActionListener { SwingUtilities.invokeLater { close() } } })
        }
        val trayIcon = TrayIcon(icon.toAwtImage(density, LayoutDirection.Ltr, Size(size, size)), title, menu).apply {
            isImageAutoSize = true
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.button == MouseEvent.BUTTON1) SwingUtilities.invokeLater { open() }
                }
            })
        }
        SystemTray.getSystemTray().add(trayIcon)
        onDispose { SystemTray.getSystemTray().remove(trayIcon) }
    }
}
