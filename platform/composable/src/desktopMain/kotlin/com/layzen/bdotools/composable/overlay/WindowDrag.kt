package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Window

/**
 * Перетаскивание окна [window] зажатой ЛКМ. Окно ставится по курсору на экране напрямую, без перерисовки на каждый
 * сдвиг — иначе оно отстаёт. [onDragEnd] получает итоговый сдвиг в логических пикселях: запомнить место — тогда.
 * Ставится раньше `clickable`, иначе клик съедает нажатие.
 */
internal fun Modifier.windowDrag(window: Window, onDragEnd: (dx: Int, dy: Int) -> Unit): Modifier = pointerInput(window) {
    var mouseStart = Point()
    var windowStart = Point()
    detectDragGestures(
        onDragStart = {
            mouseStart = MouseInfo.getPointerInfo()?.location ?: Point()
            windowStart = window.location
        },
        onDragEnd = { onDragEnd(window.x - windowStart.x, window.y - windowStart.y) },
    ) { change, _ ->
        change.consume()
        val mouse = MouseInfo.getPointerInfo()?.location ?: return@detectDragGestures
        window.setLocation(windowStart.x + mouse.x - mouseStart.x, windowStart.y + mouse.y - mouseStart.y)
    }
}
