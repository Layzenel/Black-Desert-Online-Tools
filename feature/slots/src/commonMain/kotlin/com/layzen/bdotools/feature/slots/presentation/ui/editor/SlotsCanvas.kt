package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Экран игры в масштабе: подложка, сетка от центра и включённые в игре слоты.
 * Пропорции холста — как у экрана игры, поэтому видно, где слоты окажутся на мониторе.
 */
@Composable
internal fun SlotsCanvas(state: SlotsState, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val screen = state.loaded?.game?.screen ?: return
    val focus = remember { FocusRequester() }
    BoxWithConstraints(
        modifier
            .aspectRatio((screen.width / screen.height).toFloat())
            .clipToBounds()
            .background(AppTheme.colors.track)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border)
            .focusRequester(focus)
            .focusable()
            .onPreviewKeyEvent { event ->
                val direction = event.arrowDirection()
                if (event.type == KeyEventType.KeyDown && direction != null) {
                    onEvent(SlotsEvent.Nudge(direction.first, direction.second, byGrid = event.isShiftPressed))
                }
                direction != null
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    focus.requestFocus()
                    onEvent(SlotsEvent.ClearSelection)
                }
            },
    ) {
        val scale = with(LocalDensity.current) { maxWidth.toPx() } / screen.width.toFloat()
        state.background?.let {
            Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        GridLayer(state.grid, scale, Modifier.fillMaxSize())
        state.visibleSlots.forEach { id ->
            val position = state.layout[id] ?: return@forEach
            SlotFrame(
                id = id,
                position = position,
                screen = screen,
                scale = scale,
                selected = id in state.selection,
                onEvent = onEvent,
                onPress = { focus.requestFocus() },
            )
        }
    }
}

/** Направление стрелки как (dx, dy) или `null`, если нажата не стрелка. */
private fun KeyEvent.arrowDirection(): Pair<Int, Int>? = when (key) {
    Key.DirectionLeft -> -1 to 0
    Key.DirectionRight -> 1 to 0
    Key.DirectionUp -> 0 to -1
    Key.DirectionDown -> 0 to 1
    else -> null
}
