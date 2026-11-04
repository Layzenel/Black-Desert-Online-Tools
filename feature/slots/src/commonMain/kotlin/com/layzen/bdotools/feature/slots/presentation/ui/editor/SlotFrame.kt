package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.domain.SlotGroup
import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotPosition
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.feature.slots.presentation.ui.slotTitle
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Рамка элемента на холсте в настоящем размере, как в режиме правки интерфейса игры: глазик сверху, номер в центре.
 * Клик выбирает элемент, Shift+клик добавляет к выбору, перетаскивание двигает все выбранные.
 * Скрытый в игре элемент нарисован приглушённым.
 */
@Composable
internal fun SlotFrame(
    id: SlotId,
    position: SlotPosition,
    screen: ScreenSpace,
    scale: Float,
    selected: Boolean,
    visible: Boolean,
    onEvent: (SlotsEvent) -> Unit,
    onPress: () -> Unit,
) {
    val border = if (selected) AppTheme.colors.textPrimary else id.group.frameColor()
    // За краем экрана — красная заливка; рамка остаётся цветом группы, чтобы было видно, что это за элемент
    val fill = if (screen.fits(id, position)) AppTheme.colors.backgroundDeep else AppTheme.colors.danger
    val density = LocalDensity.current
    val iconWidth = with(density) { (id.width * scale).toDp() }
    // У больших окон глазик в игре такой же, как у слота, а не во всю ширину
    val eyeSize = with(density) { (minOf(id.width, id.height, SlotGroup.Cooldown.width) * scale).toDp() } * SlotsDimens.EYE_SHARE
    Box(
        Modifier
            .offset {
                IntOffset(
                    (screen.iconLeft(id, position) * scale).roundToInt(),
                    (screen.iconTop(id, position) * scale).roundToInt(),
                )
            }
            .size(iconWidth, with(density) { (id.height * scale).toDp() }),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .alpha(if (visible) 1f else SlotsDimens.HIDDEN_ALPHA)
                .background(fill.copy(alpha = SlotsDimens.SLOT_FILL_ALPHA))
                .border(if (selected) SlotsDimens.selectedSlotBorder else SlotsDimens.slotBorder, border)
                .pointerHoverIcon(PointerIcon.Hand)
                .slotGestures(id, scale, onEvent, onPress),
            contentAlignment = Alignment.Center,
        ) {
            val label = when (id.group) {
                SlotGroup.ClassResource -> null
                SlotGroup.Interface -> slotTitle(id)
                else -> id.number.toString()
            }
            if (label != null && iconWidth >= SlotsDimens.labelMinIconWidth) {
                Text(
                    label,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
            }
        }
        // Глаз внутри рамки сверху по центру, как в режиме правки интерфейса игры
        EyeToggle(
            visible = visible,
            eyeSize = eyeSize,
            onToggle = { onEvent(SlotsEvent.ToggleVisibility(id)) },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = eyeSize * SlotsDimens.EYE_TOP_SHARE),
        )
    }
}

/** Нажатие без сдвига — выбор, со сдвигом больше порога — перетаскивание. Нажатие поглощается. */
@Composable
private fun Modifier.slotGestures(id: SlotId, scale: Float, onEvent: (SlotsEvent) -> Unit, onPress: () -> Unit): Modifier {
    val currentScale by rememberUpdatedState(scale)
    val currentOnEvent by rememberUpdatedState(onEvent)
    return pointerInput(id) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
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
