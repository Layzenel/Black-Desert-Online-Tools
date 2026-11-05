package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.slots.domain.ScreenArea
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.uikit.components.PanCursor
import com.layzen.bdotools.uikit.components.onSecondaryClick
import com.layzen.bdotools.uikit.components.panCursor
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Окно с экраном игры в масштабе, общее для редактора и превью. Окно в пропорциях экрана [screen], самое большое из
 * влезающих. Колесо мыши меняет масштаб к точке под курсором; зажатая ЛКМ по пустому месту обводит рамкой —
 * [onArea] получает рамку в UI-px экрана и признак «добавить» (Ctrl); Shift, Пробел или средняя кнопка двигают
 * холст, пока они зажаты, курсор — рука; клик по пустому месту — [onTap]. Клавиши, кроме Shift и Пробела, отдаются [onKey].
 * [content] получает масштаб: UI-px × масштаб = dp; [overlay] рисуется поверх окна, не сдвигаясь вместе с холстом.
 * [onSecondaryClick] — правый клик: точка в пикселях окна (для меню) и она же в UI-px экрана (пустая рамка в этой точке).
 */
@Composable
internal fun ScreenCanvas(
    screen: ScreenSpace,
    viewport: CanvasViewport,
    focus: FocusRequester,
    onKey: (KeyEvent) -> Boolean,
    onTap: () -> Unit,
    onArea: (ScreenArea, additive: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSecondaryClick: ((Offset, ScreenArea) -> Unit)? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.(scale: Float) -> Unit,
) {
    val currentOnKey by rememberUpdatedState(onKey)
    val currentOnSecondaryClick by rememberUpdatedState(onSecondaryClick)
    var marquee by remember { mutableStateOf<Rect?>(null) }
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
    BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
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
                    currentOnKey(event)
                }
                .hoverable(pointerOver)
                .panCursor(cursor)
                .then(
                    if (onSecondaryClick == null) Modifier
                    else Modifier.onSecondaryClick { at -> currentOnSecondaryClick?.invoke(at, Rect(at, at).toScreenArea(viewport, currentScale)) },
                )
                .middleButtonPan(viewport) { panning = it }
                .wheelZoom(viewport)
                .canvasGestures(
                    viewport,
                    panKeyHeld = { panKeyHeld },
                    onPanning = { panning = it },
                    onTap = {
                        focus.requestFocus()
                        onTap()
                    },
                    onMarquee = { marquee = it },
                    onArea = { rect, additive ->
                        focus.requestFocus()
                        onArea(rect.toScreenArea(viewport, currentScale), additive)
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
                content(scale)
            }
            marquee?.let { MarqueeFrame(it) }
            overlay()
        }
    }
}

/** Прямоугольник в пикселях окна → UI-px экрана игры: убираем сдвиг холста и делим на масштаб. */
private fun Rect.toScreenArea(viewport: CanvasViewport, scale: Float) = ScreenArea.between(
    ((left - viewport.pan.x) / scale).toDouble(),
    ((top - viewport.pan.y) / scale).toDouble(),
    ((right - viewport.pan.x) / scale).toDouble(),
    ((bottom - viewport.pan.y) / scale).toDouble(),
)
