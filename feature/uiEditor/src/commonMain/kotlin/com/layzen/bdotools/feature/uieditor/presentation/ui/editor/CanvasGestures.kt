package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.uieditor.presentation.ui.handleHistoryKeys
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
 * Нажатие на пустое место: без сдвига — [onTap], со сдвигом — обводка рамкой, а с Shift или при [panKeyHeld] (Пробел) — перетаскивание холста.
 * Пока тянется обводка, [onMarquee] получает её прямоугольник в пикселях окна, по отпускании — `null`,
 * а [onArea] — итоговый прямоугольник и признак «добавить к выбору» (Ctrl). [onPanning] сообщает, что холст «схвачен» для сдвига. Нажатия на элементы сюда не доходят: элементы их поглощают.
 */
internal fun Modifier.canvasGestures(
    viewport: CanvasViewport,
    panKeyHeld: () -> Boolean,
    onPanning: (Boolean) -> Unit,
    onTap: () -> Unit,
    onMarquee: (Rect?) -> Unit,
    onArea: (Rect, additive: Boolean) -> Unit,
): Modifier = pointerInput(viewport) {
    awaitEachGesture {
        val down = awaitFirstDown()
        val modifiers = currentEvent.keyboardModifiers
        val pan = modifiers.isShiftPressed || panKeyHeld()
        val additive = modifiers.isCtrlPressed || modifiers.isMetaPressed
        onPanning(pan)
        try {
            var last = down.position
            val move = { change: Offset, delta: Offset ->
                last = change
                if (pan) viewport.panBy(delta) else onMarquee(rectBetween(down.position, change))
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
            if (!pan) onArea(rectBetween(down.position, last), additive)
            onMarquee(null)
        } finally {
            onPanning(false)
        }
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

/**
 * Средняя кнопка мыши (колесо) двигает холст так же, как Shift или Пробел с ЛКМ, где бы её ни нажали, и поверх элементов.
 * Нажатие поглощается до дочерних элементов, поэтому они его не видят. [onPanning] сообщает, зажата ли кнопка.
 */
internal fun Modifier.middleButtonPan(viewport: CanvasViewport, onPanning: (Boolean) -> Unit): Modifier = pointerInput(viewport) {
    awaitPointerEventScope {
        while (true) {
            val press = awaitPointerEvent(PointerEventPass.Initial)
            if (press.type != PointerEventType.Press || !press.buttons.isTertiaryPressed) continue
            press.changes.forEach { it.consume() }
            onPanning(true)
            try {
                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.first()
                    viewport.panBy(change.positionChange())
                    change.consume()
                } while (event.buttons.isTertiaryPressed)
            } finally {
                onPanning(false)
            }
        }
    }
}
