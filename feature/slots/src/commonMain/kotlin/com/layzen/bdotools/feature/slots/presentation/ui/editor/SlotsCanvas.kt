package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/** Во сколько раз меняется масштаб за один щелчок колеса. */
private const val WHEEL_ZOOM_STEP = 1.15f

/**
 * Экран игры в масштабе: подложка, сетка от прицела, прицел и элементы интерфейса.
 * Окно холста всегда в пропорциях экрана игры. Колесо мыши меняет масштаб к точке под курсором,
 * зажатая ЛКМ по пустому месту двигает холст, клик по пустому месту снимает выбор.
 */
@Composable
internal fun SlotsCanvas(state: SlotsState, viewport: CanvasViewport, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val screen = state.loaded?.game?.screen ?: return
    val focus = remember { FocusRequester() }
    val currentOnEvent by rememberUpdatedState(onEvent)
    BoxWithConstraints(modifier) {
        val viewportWidth = maxWidth
        val viewportHeight = maxWidth * (screen.height / screen.width).toFloat()
        Box(
            Modifier
                .size(viewportWidth, viewportHeight)
                .clipToBounds()
                .background(AppTheme.colors.track)
                .border(AppTheme.dimens.borderWidth, AppTheme.colors.border)
                .onSizeChanged { viewport.size = Size(it.width.toFloat(), it.height.toFloat()) }
                .focusRequester(focus)
                .focusable()
                .onPreviewKeyEvent { event -> event.handleArrows(currentOnEvent) }
                .wheelZoom(viewport)
                .panOrClear(viewport) {
                    focus.requestFocus()
                    currentOnEvent(SlotsEvent.ClearSelection)
                },
        ) {
            val zoom = viewport.zoom
            val scale = with(LocalDensity.current) { (viewportWidth * zoom).toPx() } / screen.width.toFloat()
            // Содержимое больше окна: снимаем ограничение родителя, иначе size() ужмёт его до размера окна
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .offset { IntOffset(viewport.pan.x.roundToInt(), viewport.pan.y.roundToInt()) }
                    .size(viewportWidth * zoom, viewportHeight * zoom),
            ) {
                CanvasContent(state, screen, scale, onEvent, onPress = { focus.requestFocus() })
            }
        }
    }
}

@Composable
private fun CanvasContent(state: SlotsState, screen: ScreenSpace, scale: Float, onEvent: (SlotsEvent) -> Unit, onPress: () -> Unit) {
    state.background?.let {
        Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
    GridLayer(state.grid, screen, scale, Modifier.fillMaxSize())
    CrosshairLayer(screen, scale, Modifier.fillMaxSize())
    state.slots.forEach { id ->
        val position = state.layout[id] ?: return@forEach
        SlotFrame(
            id = id,
            position = position,
            screen = screen,
            scale = scale,
            selected = id in state.selection,
            visible = state.layout.isVisible(id),
            onEvent = onEvent,
            onPress = onPress,
        )
    }
}

/** Колесо мыши: вверх — ближе, вниз — дальше, к точке под курсором. */
private fun Modifier.wheelZoom(viewport: CanvasViewport): Modifier = pointerInput(viewport) {
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
 * Нажатие на пустое место: без сдвига — [onTap], со сдвигом — перетаскивание холста.
 * Нажатия на элементы сюда не доходят: элементы их поглощают.
 */
private fun Modifier.panOrClear(viewport: CanvasViewport, onTap: () -> Unit): Modifier = pointerInput(viewport) {
    awaitEachGesture {
        val down = awaitFirstDown()
        var dragged = false
        val dragStart = awaitTouchSlopOrCancellation(down.id) { change, over ->
            change.consume()
            dragged = true
            viewport.panBy(over)
        }
        if (dragStart == null) {
            if (!dragged) onTap()
            return@awaitEachGesture
        }
        drag(dragStart.id) { change ->
            viewport.panBy(change.positionChange())
            change.consume()
        }
    }
}

/** Стрелки двигают выбранные элементы: на 1 UI-px, с Shift — на шаг привязки. */
private fun KeyEvent.handleArrows(onEvent: (SlotsEvent) -> Unit): Boolean {
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
