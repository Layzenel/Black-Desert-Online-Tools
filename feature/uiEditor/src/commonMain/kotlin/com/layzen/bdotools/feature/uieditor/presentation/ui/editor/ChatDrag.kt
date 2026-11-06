package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent
import kotlin.math.roundToInt

/**
 * Жест над окном чата: на каждом шаге [step] получает весь сдвиг от начала жеста в UI-px и даёт событие для него —
 * контроллер считает окно от раскладки на начало жеста, поэтому упор в предел и привязки не копят ошибку.
 * Конец жеста — [SlotsEvent.ChatEditEnd]. [onDragging] сообщает, идёт ли жест. Нажатие поглощается.
 */
@Composable
internal fun Modifier.chatDrag(
    scale: Float,
    onEvent: (SlotsEvent) -> Unit,
    onDragging: (Boolean) -> Unit = {},
    step: (dx: Int, dy: Int) -> SlotsEvent,
): Modifier {
    val currentScale by rememberUpdatedState(scale)
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentStep by rememberUpdatedState(step)
    return pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            var total = Offset.Zero
            onDragging(true)
            drag(down.id) { pointer ->
                total += pointer.positionChange()
                pointer.consume()
                currentOnEvent(currentStep((total.x / currentScale).roundToInt(), (total.y / currentScale).roundToInt()))
            }
            onDragging(false)
            currentOnEvent(SlotsEvent.ChatEditEnd)
        }
    }
}
