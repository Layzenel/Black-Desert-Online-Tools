package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.ui.handleHistoryKeys
import kotlin.math.max
import kotlin.math.min

/** Во сколько раз меняется масштаб за один щелчок колеса. */
private const val WHEEL_ZOOM_STEP = 1.15f

/** Колесо мыши: вверх — ближе, вниз — дальше, к точке под курсором. */
internal fun Modifier.wheelZoom(viewport: CanvasViewport): Modifier = pointerInput(viewport) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.type != PointerEventType.Scroll) continue
            val change = event.changes.first()
            val factor = if (change.scrollDelta.y < 0) WHEEL_ZOOM_STEP else 1 / WHEEL_ZOOM_STEP
            viewport.zoomAt(change.position, factor)
            change.consume()
        }
    }
}

/**
 * Нажатие на пустое место: без сдвига — [onTap], со сдвигом — перетаскивание холста, а с Shift — обводка рамкой.
 * Пока тянется обводка, [onMarquee] получает её прямоугольник в пикселях окна, по отпускании — `null`,
 * а [onArea] — итоговый прямоугольник. Нажатия на элементы сюда не доходят: элементы их поглощают.
 */
internal fun Modifier.canvasGestures(
    viewport: CanvasViewport,
    onTap: () -> Unit,
    onMarquee: (Rect?) -> Unit,
    onArea: (Rect) -> Unit,
): Modifier = pointerInput(viewport) {
    awaitEachGesture {
        val down = awaitFirstDown()
        val marquee = currentEvent.keyboardModifiers.isShiftPressed
        var last = down.position
        val move = { change: Offset, delta: Offset ->
            last = change
            if (marquee) onMarquee(rectBetween(down.position, change)) else viewport.panBy(delta)
        }
        var dragged = false
        val dragStart = awaitTouchSlopOrCancellation(down.id) { change, over ->
            change.consume()
            dragged = true
            move(change.position, over)
        }
        if (dragStart == null) {
            if (!dragged) onTap()
            onMarquee(null)
            return@awaitEachGesture
        }
        drag(dragStart.id) { change ->
            move(change.position, change.positionChange())
            change.consume()
        }
        if (marquee) onArea(rectBetween(down.position, last))
        onMarquee(null)
    }
}

/**
 * Стрелки двигают выбранные элементы: на 1 UI-px, с Shift — на шаг привязки. Esc снимает выбор.
 * Ctrl+Z отменяет правку, Ctrl+Shift+Z и Ctrl+Y повторяют её.
 */
internal fun KeyEvent.handleKeys(onEvent: (SlotsEvent) -> Unit): Boolean {
    if (handleHistoryKeys(onUndo = { onEvent(SlotsEvent.Undo) }, onRedo = { onEvent(SlotsEvent.Redo) })) return true
    if (key == Key.Escape) {
        if (type == KeyEventType.KeyDown) onEvent(SlotsEvent.ClearSelection)
        return true
    }
    val direction = when (key) {
        Key.DirectionLeft -> -1 to 0
        Key.DirectionRight -> 1 to 0
        Key.DirectionUp -> 0 to -1
        Key.DirectionDown -> 0 to 1
        else -> return false
    }
    if (type == KeyEventType.KeyDown) onEvent(SlotsEvent.Nudge(direction.first, direction.second, byGrid = isShiftPressed))
    return true
}

/** Прямоугольник между двумя точками в любом порядке: рамку можно тянуть в любую сторону. */
internal fun rectBetween(a: Offset, b: Offset) = Rect(Offset(min(a.x, b.x), min(a.y, b.y)), Offset(max(a.x, b.x), max(a.y, b.y)))
