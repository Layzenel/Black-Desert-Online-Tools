package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotPosition
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Рамка слота на холсте, как в режиме правки интерфейса игры: номер в центре.
 * Клик выбирает слот, Shift+клик добавляет к выбору, перетаскивание двигает все выбранные.
 */
@Composable
internal fun SlotFrame(
    id: SlotId,
    position: SlotPosition,
    screen: ScreenSpace,
    scale: Float,
    selected: Boolean,
    onEvent: (SlotsEvent) -> Unit,
    onPress: () -> Unit,
) {
    val fits = screen.fits(id, position)
    val border = when {
        selected -> AppTheme.colors.accentGold
        !fits -> AppTheme.colors.danger
        else -> id.group.frameColor()
    }
    val density = LocalDensity.current
    Box(
        Modifier
            .offset {
                IntOffset(
                    (screen.frameLeft(id.group, position) * scale).roundToInt(),
                    (screen.frameTop(id.group, position) * scale).roundToInt(),
                )
            }
            .size(with(density) { (id.group.width * scale).toDp() }, with(density) { (id.group.height * scale).toDp() })
            .background(AppTheme.colors.backgroundDeep.copy(alpha = SlotsDimens.SLOT_FILL_ALPHA))
            .border(if (selected) SlotsDimens.selectedSlotBorder else SlotsDimens.slotBorder, border)
            .pointerHoverIcon(PointerIcon.Hand)
            .slotGestures(id, scale, onEvent, onPress),
        contentAlignment = Alignment.Center,
    ) {
        Text(id.number.toString(), style = AppTheme.typography.caption, color = AppTheme.colors.textPrimary)
    }
}

/**
 * Нажатие без сдвига — выбор, со сдвигом больше порога — перетаскивание. Нажатие поглощается,
 * чтобы холст под слотом не снял выбор.
 */
@Composable
private fun Modifier.slotGestures(id: SlotId, scale: Float, onEvent: (SlotsEvent) -> Unit, onPress: () -> Unit): Modifier {
    val currentScale by rememberUpdatedState(scale)
    val currentOnEvent by rememberUpdatedState(onEvent)
    return pointerInput(id) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            onPress()
            val additive = currentEvent.keyboardModifiers.isShiftPressed
            var started = false
            val dragChange = awaitTouchSlopOrCancellation(down.id) { change, over ->
                change.consume()
                started = true
                currentOnEvent(SlotsEvent.DragStart(id, additive))
                currentOnEvent(SlotsEvent.DragBy(over.x / currentScale, over.y / currentScale))
            }
            if (dragChange == null) {
                currentOnEvent(if (started) SlotsEvent.DragEnd else SlotsEvent.Select(id, additive))
                return@awaitEachGesture
            }
            drag(dragChange.id) { change ->
                val delta = change.positionChange()
                change.consume()
                currentOnEvent(SlotsEvent.DragBy(delta.x / currentScale, delta.y / currentScale))
            }
            currentOnEvent(SlotsEvent.DragEnd)
        }
    }
}
