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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import com.layzen.bdotools.uikit.components.PanCursor
import com.layzen.bdotools.uikit.components.onSecondaryClick
import com.layzen.bdotools.uikit.components.panCursor
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Экран игры в масштабе: подложка, сетка от прицела, прицел и элементы интерфейса.
 * Окно холста всегда в пропорциях экрана игры. Колесо мыши меняет масштаб к точке под курсором,
 * зажатая ЛКМ по пустому месту обводит элементы рамкой (с Ctrl — добавляет к выбору), с Shift или Пробелом двигает холст,
 * ПКМ открывает меню; клик по пустому месту и Esc
 * снимают выбор.
 */
@Composable
internal fun SlotsCanvas(state: SlotsState, viewport: CanvasViewport, onEvent: (SlotsEvent) -> Unit, modifier: Modifier = Modifier) {
    val screen = state.loaded?.game?.screen ?: return
    val focus = remember { FocusRequester() }
    val currentOnEvent by rememberUpdatedState(onEvent)
    var marquee by remember { mutableStateOf<Rect?>(null) }
    var menuAt by remember { mutableStateOf<Offset?>(null) }
    // Shift или Пробел зажаты: ЛКМ двигает холст, курсор — рука
    var spaceHeld by remember { mutableStateOf(false) }
    var panKeyHeld by remember { mutableStateOf(false) }
    var panning by remember { mutableStateOf(false) }
    val pointerOver = remember { MutableInteractionSource() }
    val pointerIsOver by pointerOver.collectIsHoveredAsState()
    val cursor = when {
        !pointerIsOver -> PanCursor.None
        panning -> PanCursor.Grabbing
        panKeyHeld -> PanCursor.Open
        else -> PanCursor.None
    }
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
                .onFocusChanged {
                    if (!it.isFocused) {
                        spaceHeld = false
                        panKeyHeld = false
                    }
                }
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Spacebar) spaceHeld = event.type == KeyEventType.KeyDown
                    panKeyHeld = spaceHeld || event.isShiftPressed
                    event.handleKeys(currentOnEvent)
                }
                .hoverable(pointerOver)
                .panCursor(cursor)
                .onSecondaryClick { menuAt = it }
                .middleButtonPan(viewport) { panning = it }
                .wheelZoom(viewport)
                .canvasGestures(
                    viewport,
                    panKeyHeld = { panKeyHeld },
                    onPanning = { panning = it },
                    onTap = {
                        focus.requestFocus()
                        currentOnEvent(SlotsEvent.ClearSelection)
                    },
                    onMarquee = { marquee = it },
                    onArea = { rect, additive ->
                        focus.requestFocus()
                        currentOnEvent(SlotsEvent.SelectArea(rect.toScreenArea(viewport, currentScale), additive))
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
            // Пустой Box в точке клика: меню раскрывается от его угла
            menuAt?.let { at ->
                Box(Modifier.offset { IntOffset(at.x.roundToInt(), at.y.roundToInt()) }) {
                    CanvasContextMenu(state, onDismiss = { menuAt = null }, onEvent = onEvent)
                }
            }
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
    SnapGuidesLayer(state.snapGuides, screen, scale, Modifier.fillMaxSize())
}

/** Прямоугольник в пикселях окна → UI-px экрана игры: убираем сдвиг холста и делим на масштаб. */
private fun Rect.toScreenArea(viewport: CanvasViewport, scale: Float) = ScreenArea.between(
    ((left - viewport.pan.x) / scale).toDouble(),
    ((top - viewport.pan.y) / scale).toDouble(),
    ((right - viewport.pan.x) / scale).toDouble(),
    ((bottom - viewport.pan.y) / scale).toDouble(),
)
