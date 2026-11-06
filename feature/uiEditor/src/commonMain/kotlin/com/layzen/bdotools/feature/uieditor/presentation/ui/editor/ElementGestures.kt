package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent

/**
 * Жест над элементом холста (слотом или окном чата) [key]: нажатие без сдвига — [select], со сдвигом больше порога —
 * [dragStart] и перетаскивание всего выбранного ([SlotsEvent.DragBy], [SlotsEvent.DragEnd]). Признак «добавить
 * к выбору» — Ctrl (на Mac — Cmd). Нажатие поглощается.
 */
@Composable
internal fun Modifier.elementGestures(
    key: Any,
    scale: Float,
    onEvent: (SlotsEvent) -> Unit,
    onPress: () -> Unit,
    select: (additive: Boolean) -> SlotsEvent,
    dragStart: (additive: Boolean) -> SlotsEvent,
): Modifier {
    val currentScale by rememberUpdatedState(scale)
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentSelect by rememberUpdatedState(select)
    val currentDragStart by rememberUpdatedState(dragStart)
    return pointerInput(key) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            val modifiers = currentEvent.keyboardModifiers
            val additive = modifiers.isCtrlPressed || modifiers.isMetaPressed
            var started = false
            val dragChange = awaitTouchSlopOrCancellation(down.id) { change, over ->
                change.consume()
                started = true
                currentOnEvent(currentDragStart(additive))
                currentOnEvent(SlotsEvent.DragBy(over.x / currentScale, over.y / currentScale))
            }
            if (dragChange == null) {
                currentOnEvent(if (started) SlotsEvent.DragEnd else currentSelect(additive))
            } else {
                drag(dragChange.id) { change ->
                    val delta = change.positionChange()
                    change.consume()
                    currentOnEvent(SlotsEvent.DragBy(delta.x / currentScale, delta.y / currentScale))
                }
                currentOnEvent(SlotsEvent.DragEnd)
            }
            // Фокус для стрелок — только после жеста: перевод фокуса во время нажатия прокручивал страницу к холсту
            // и сбивал первое перетаскивание
            onPress()
        }
    }
}
