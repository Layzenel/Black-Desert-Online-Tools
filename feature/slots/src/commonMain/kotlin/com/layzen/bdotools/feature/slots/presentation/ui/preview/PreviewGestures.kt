package com.layzen.bdotools.feature.slots.presentation.ui.preview

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import com.layzen.bdotools.feature.slots.presentation.ui.editor.rectBetween

/**
 * Shift и протянуть по превью — обводка рамкой. Пока её тянут, [onMarquee] получает прямоугольник в пикселях
 * превью, по отпускании — `null`, а [onArea] — итоговый прямоугольник. Без Shift нажатие ничего не делает:
 * у превью нет сдвига и масштаба, а клики по элементам поглощают сами элементы.
 */
internal fun Modifier.marqueeGesture(onMarquee: (Rect?) -> Unit, onArea: (Rect) -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown()
        if (!currentEvent.keyboardModifiers.isShiftPressed) return@awaitEachGesture
        var last = down.position
        val dragStart = awaitTouchSlopOrCancellation(down.id) { change, _ ->
            change.consume()
            last = change.position
            onMarquee(rectBetween(down.position, last))
        }
        if (dragStart == null) {
            onMarquee(null)
            return@awaitEachGesture
        }
        drag(dragStart.id) { change ->
            last = change.position
            onMarquee(rectBetween(down.position, last))
            change.consume()
        }
        onArea(rectBetween(down.position, last))
        onMarquee(null)
    }
}
