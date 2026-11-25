package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.layzen.bdotools.composable.overlay.rememberOverlayShown
import com.layzen.bdotools.composable.overlay.setClickThrough
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep
import java.awt.GraphicsEnvironment

/**
 * Окно сетки на весь основной экран: без рамки, фокуса и мыши. Игра берётся полноэкранной на основном мониторе
 * (оконный режим сетку не сдвигает вслед за окном игры).
 */
@Composable
internal actual fun GameGridWindow(step: GridStep, uiHeight: Double) {
    val shown by rememberOverlayShown()
    val screen = remember { GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds }
    val state = rememberWindowState(position = WindowPosition(screen.x.dp, screen.y.dp), size = DpSize(screen.width.dp, screen.height.dp))
    Window(
        onCloseRequest = {},
        state = state,
        visible = shown,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        LaunchedEffect(window) { setClickThrough(window, enabled = true) }
        GridLines(step, uiHeight)
    }
}
