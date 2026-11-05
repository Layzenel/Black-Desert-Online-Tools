package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.slots.domain.ScreenArea
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.presentation.event.SlotsEvent
import com.layzen.bdotools.feature.slots.presentation.state.SlotsState
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Экран игры в масштабе: подложка, сетка от прицела, прицел и элементы интерфейса.
 * Окно холста всегда в пропорциях экрана игры. Колесо мыши меняет масштаб к точке под курсором,
 * зажатая ЛКМ по пустому месту двигает холст, с Shift — обводит элементы рамкой; клик по пустому месту и Esc
 * снимают выбор.
 */
@Composable
internal fun SlotsCanvas(state: SlotsState, viewport: CanvasViewport, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val screen = state.loaded?.game?.screen ?: return
    val focus = remember { FocusRequester() }
    val currentOnEvent by rememberUpdatedState(onEvent)
    var marquee by remember { mutableStateOf<Rect?>(null) }
    // Фокус сразу на холсте: Ctrl+Z и стрелки работают без клика по нему
    LaunchedEffect(focus) { focus.requestFocus() }
    BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
        // Самое большое окно в пропорциях экрана игры, которое влезает в отведённое место
        val ratio = (screen.height / screen.width).toFloat()
        val viewportWidth = if (maxHeight == Dp.Infinity) maxWidth else minOf(maxWidth, maxHeight / ratio)
        val viewportHeight = viewportWidth * ratio
        val zoom = viewport.zoom
        val scale = with(LocalDensity.current) { (viewportWidth * zoom).toPx() } / screen.width.toFloat()
        val currentScale by rememberUpdatedState(scale)
        Box(
            Modifier
                .size(viewportWidth, viewportHeight)
                .clipToBounds()
                .background(AppTheme.colors.track)
                .border(AppTheme.dimens.borderWidth, AppTheme.colors.border)
                .onSizeChanged { viewport.size = Size(it.width.toFloat(), it.height.toFloat()) }
                .focusRequester(focus)
                .focusable()
                .onPreviewKeyEvent { event -> event.handleKeys(currentOnEvent) }
                .wheelZoom(viewport)
                .canvasGestures(
                    viewport,
                    onTap = {
                        focus.requestFocus()
                        currentOnEvent(SlotsEvent.ClearSelection)
                    },
                    onMarquee = { marquee = it },
                    onArea = { rect ->
                        focus.requestFocus()
                        currentOnEvent(SlotsEvent.SelectArea(rect.toScreenArea(viewport, currentScale), additive = true))
                    },
                ),
        ) {
            // Содержимое больше окна: снимаем ограничение родителя, иначе size() ужмёт его до размера окна
            Box(
                Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .offset { IntOffset(viewport.pan.x.roundToInt(), viewport.pan.y.roundToInt()) }
                    .size(viewportWidth * zoom, viewportHeight * zoom),
            ) {
                CanvasContent(state, screen, scale, onEvent, onPress = { focus.requestFocus() })
            }
            marquee?.let { MarqueeFrame(it) }
        }
    }
}

@Composable
private fun CanvasContent(state: SlotsState, screen: ScreenSpace, scale: Float, onEvent: (SlotsEvent) -> Unit, onPress: () -> Unit) {
    state.background?.takeIf { state.backgroundShown }?.let {
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

/** Прямоугольник в пикселях окна → UI-px экрана игры: убираем сдвиг холста и делим на масштаб. */
private fun Rect.toScreenArea(viewport: CanvasViewport, scale: Float) = ScreenArea.between(
    ((left - viewport.pan.x) / scale).toDouble(),
    ((top - viewport.pan.y) / scale).toDouble(),
    ((right - viewport.pan.x) / scale).toDouble(),
    ((bottom - viewport.pan.y) / scale).toDouble(),
)
