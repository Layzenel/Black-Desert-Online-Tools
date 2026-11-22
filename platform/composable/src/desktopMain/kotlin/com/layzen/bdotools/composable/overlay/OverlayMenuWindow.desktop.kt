package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Пауза перед подъёмом окна: окно оверлея должно успеть показаться, иначе оно окажется выше. */
internal const val RAISE_DELAY_MS = 100L

/**
 * Окно кнопки меню: своё место на экране, независимое от оверлея и от самого меню ([savedMenuPosition]). Прозрачное,
 * без рамки, поверх всех окон и всегда над оверлеем ([OverlayLayers]), видно только пока игра на переднем плане.
 * Без фокуса: клик по кнопке не уводит игру с переднего плана. Тянется зажатой ЛКМ. Пока курсор спрятан игрой,
 * клики проходят сквозь окно в игру.
 */
@Composable
internal actual fun OverlayMenuWindow(open: Boolean, content: @Composable (dragModifier: Modifier, gameElevated: Boolean) -> Unit) {
    val gameActive by rememberGameActive()
    // права у запущенной игры не меняются: проверка — при каждом её выходе на передний план
    val gameElevated by produceState(false, gameActive) {
        if (gameActive) value = withContext(Dispatchers.IO) { isGameElevated() }
    }
    val state = rememberWindowState(
        position = remember { savedMenuPosition() },
        size = DpSize(OverlayDimens.menuButton, OverlayDimens.menuButton),
    )
    Window(
        onCloseRequest = {},
        state = state,
        visible = gameActive,
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = false,
        alwaysOnTop = true,
    ) {
        // пока игра прячет курсор (режим боя), клики уходят в игру: атака не достаётся кнопке
        val cursorShown by rememberCursorShown()
        LaunchedEffect(window, cursorShown) { setClickThrough(window, enabled = !cursorShown) }
        LaunchedEffect(gameActive, open, OverlayLayers.overlayShown) {
            delay(RAISE_DELAY_MS)
            if (gameActive) window.toFront()
        }
        LaunchedEffect(state) {
            snapshotFlow { state.position }.collect { if (it is WindowPosition.Absolute) savePosition(MENU_ID, it.x, it.y) }
        }
        content(Modifier.windowDrag(window) { _, _ -> state.position = WindowPosition(window.x.dp, window.y.dp) }, gameElevated)
    }
}
