package com.layzen.bdotools.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import com.layzen.bdotools.navigation.AppTab
import java.awt.MouseInfo
import java.awt.Point
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

/**
 * Значок программы в трее. ЛКМ — показать окно ([onOpen]); ПКМ — меню в стиле программы ([TrayMenuWindow]):
 * вкладки ([onOpenTab] открывает вкладку и окно) и «Закрыть» ([onExit]). Свой `TrayIcon`, а не `Tray` из Compose:
 * тот открывает окно двойным кликом, а нужно одиночным; системное `PopupMenu` не красится в цвета темы.
 */
@Composable
internal fun AppTray(icon: Painter, title: String, onOpen: () -> Unit, onOpenTab: (AppTab) -> Unit, onExit: () -> Unit) {
    if (!SystemTray.isSupported()) return
    val density = LocalDensity.current
    val open by rememberUpdatedState(onOpen)
    var menuAt by remember { mutableStateOf<Point?>(null) }
    DisposableEffect(Unit) {
        val size = WindowDimens.trayIconPx.toFloat()
        val trayIcon = TrayIcon(icon.toAwtImage(density, LayoutDirection.Ltr, Size(size, size)), title).apply {
            isImageAutoSize = true
            addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(e: MouseEvent) {
                    // у событий TrayIcon нет компонента, locationOnScreen там (0, 0) — берём сам курсор
                    val at = MouseInfo.getPointerInfo().location
                    when (e.button) {
                        MouseEvent.BUTTON1 -> SwingUtilities.invokeLater { menuAt = null; open() }
                        MouseEvent.BUTTON3 -> SwingUtilities.invokeLater { menuAt = at }
                    }
                }
            })
        }
        SystemTray.getSystemTray().add(trayIcon)
        onDispose { SystemTray.getSystemTray().remove(trayIcon) }
    }
    menuAt?.let { at ->
        // key: новый ПКМ ставит меню к новому месту курсора заново
        key(at) {
            TrayMenuWindow(
                at = at,
                onOpenTab = { menuAt = null; onOpenTab(it) },
                onExit = onExit,
                onDismiss = { menuAt = null },
            )
        }
    }
}
