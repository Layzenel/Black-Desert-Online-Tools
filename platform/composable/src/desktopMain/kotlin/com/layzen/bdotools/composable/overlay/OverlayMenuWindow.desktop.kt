package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.awt.MouseInfo
import java.awt.Point
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay

/** Пауза перед подъёмом окна: окно оверлея должно успеть показаться, иначе оно окажется выше. */
private const val RAISE_DELAY_MS = 100L

/**
 * Окно кнопки меню: своё место на экране, независимое от оверлея ([savedMenuPosition]). Прозрачное, без рамки,
 * поверх всех окон и всегда над оверлеем ([OverlayLayers]), видно только пока игра на переднем плане. Размером
 * с содержимое (раскрытое меню растит окно вниз и вправо). Без фокуса: клик по кнопке не уводит игру с переднего
 * плана. Тянется зажатой ЛКМ. Пока курсор спрятан игрой, клики проходят сквозь окно в игру.
 */
@Composable
internal actual fun OverlayMenuWindow(open: Boolean, content: @Composable (dragModifier: Modifier) -> Unit) {
    val gameActive by rememberGameActive()
    val start = DpSize(OverlayDimens.menuButton, OverlayDimens.menuButton)
    val state = rememberWindowState(position = remember { savedMenuPosition() }, size = start)
    // только поверх игры: окно кнопки фокус не берёт, так что клик по ней игру с переднего плана не уводит
    val shown = gameActive
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
        // пока игра прячет курсор (режим боя), клики уходят в игру: атака не достаётся кнопке
        val cursorShown by rememberCursorShown()
        LaunchedEffect(window, cursorShown) { setClickThrough(window, enabled = !cursorShown) }
        LaunchedEffect(shown, open, OverlayLayers.overlayShown) {
            delay(RAISE_DELAY_MS)
            if (shown) window.toFront()
        }
        LaunchedEffect(state) {
            snapshotFlow { state.position }.collect { position ->
                if (position is WindowPosition.Absolute) savePosition(MENU_ID, position.x, position.y)
            }
        }
        val density = LocalDensity.current
        val fit = Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
            .onSizeChanged { state.size = with(density) { DpSize(it.width.toDp(), it.height.toDp()) } }
        // окно ставится по курсору на экране напрямую, без перерисовки на каждый сдвиг — иначе кнопка отстаёт;
        // позиция в состоянии окна (и в настройках) — когда отпустили
        val drag = Modifier.pointerInput(window) {
            var mouseStart = Point()
            var windowStart = Point()
            detectDragGestures(
                onDragStart = {
                    mouseStart = MouseInfo.getPointerInfo()?.location ?: Point()
                    windowStart = window.location
                },
                onDragEnd = { state.position = WindowPosition(window.x.dp, window.y.dp) },
            ) { change, _ ->
                change.consume()
                val mouse = MouseInfo.getPointerInfo()?.location ?: return@detectDragGestures
                window.setLocation(windowStart.x + mouse.x - mouseStart.x, windowStart.y + mouse.y - mouseStart.y)
            }
        }
        Box(fit) { content(drag) }
    }
}

