package com.layzen.bdotools.composable.canvas

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

/** Во сколько раз меняется масштаб за один щелчок колеса. */
private const val WHEEL_ZOOM_STEP = 1.15f

/** Колесо мыши: вверх — ближе, вниз — дальше, к точке под курсором. */
fun Modifier.wheelZoom(viewport: CanvasViewport): Modifier = pointerInput(viewport) {
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
 * Средняя кнопка мыши (колесо) двигает холст, где бы её ни нажали, и поверх элементов.
 * Нажатие поглощается до дочерних элементов, поэтому они его не видят. [onPanning] сообщает, зажата ли кнопка.
 */
fun Modifier.middleButtonPan(viewport: CanvasViewport, onPanning: (Boolean) -> Unit = {}): Modifier = pointerInput(viewport) {
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
