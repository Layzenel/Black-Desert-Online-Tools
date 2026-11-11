package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.layzen.bdotools.feature.uieditor.presentation.event.SlotsEvent

/**
 * Жест над элементом холста (слотом или окном чата) [key]: нажатие без сдвига — [select], со сдвигом больше порога —
 * [dragStart] и перетаскивание всего выбранного ([SlotsEvent.DragBy], [SlotsEvent.DragEnd]). Признак «добавить
 * к выбору» — Ctrl (на Mac — Cmd). Нажатие поглощается.
 *
 * [skillDragStart] — у слота перезарядки с навыком: Shift при нажатии и сдвиг тащат не слот, а навык
 * ([SlotsEvent.SkillDragBy], [SlotsEvent.SkillDragEnd]). [doubleClick] — второе нажатие без сдвига вскоре после
 * первого (выбор навыка у слота перезарядки).
 */
@Composable
internal fun Modifier.elementGestures(
    key: Any,
    scale: Float,
    onEvent: (SlotsEvent) -> Unit,
    onPress: () -> Unit,
    select: (additive: Boolean) -> SlotsEvent,
    dragStart: (additive: Boolean) -> SlotsEvent,
    skillDragStart: (() -> SlotsEvent)? = null,
    doubleClick: (() -> SlotsEvent)? = null,
): Modifier {
    val currentScale by rememberUpdatedState(scale)
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentSelect by rememberUpdatedState(select)
    val currentDragStart by rememberUpdatedState(dragStart)
    val currentSkillDragStart by rememberUpdatedState(skillDragStart)
    val currentDoubleClick by rememberUpdatedState(doubleClick)
    return pointerInput(key) {
        var lastClickAt = 0L
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            val modifiers = currentEvent.keyboardModifiers
            val additive = modifiers.isCtrlPressed || modifiers.isMetaPressed
            val skillDrag = currentSkillDragStart?.takeIf { modifiers.isShiftPressed }
            val moved: (Offset) -> SlotsEvent = { delta ->
                if (skillDrag != null) SlotsEvent.SkillDragBy(delta.x / currentScale, delta.y / currentScale)
                else SlotsEvent.DragBy(delta.x / currentScale, delta.y / currentScale)
            }
            val end = if (skillDrag != null) SlotsEvent.SkillDragEnd else SlotsEvent.DragEnd
            var started = false
            val dragChange = awaitTouchSlopOrCancellation(down.id) { change, over ->
                change.consume()
                started = true
                currentOnEvent(skillDrag?.invoke() ?: currentDragStart(additive))
                currentOnEvent(moved(over))
            }
            when {
                dragChange != null -> {
                    drag(dragChange.id) { change ->
                        val delta = change.positionChange()
                        change.consume()
                        currentOnEvent(moved(delta))
                    }
                    currentOnEvent(end)
                }
                started -> currentOnEvent(end)
                else -> {
                    val doubled = down.uptimeMillis - lastClickAt <= viewConfiguration.doubleTapTimeoutMillis
                    lastClickAt = if (doubled) 0L else down.uptimeMillis
                    currentOnEvent(currentDoubleClick?.takeIf { doubled }?.invoke() ?: currentSelect(additive))
                }
            }
            // Фокус для стрелок — только после жеста: перевод фокуса во время нажатия прокручивал страницу к холсту
            // и сбивал первое перетаскивание
            onPress()
        }
    }
}
