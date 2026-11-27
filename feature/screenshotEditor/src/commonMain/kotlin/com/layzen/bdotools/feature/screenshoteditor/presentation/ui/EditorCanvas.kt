package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.composable.canvas.CanvasViewport
import com.layzen.bdotools.composable.canvas.middleButtonPan
import com.layzen.bdotools.composable.canvas.wheelZoom
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.bounds
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.EditPainter
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.hitIndex
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState

/**
 * Видимая часть скриншота с правками, вписанная в область: после обрезки — только она. Инструменты рисуют мышью,
 * клик по правке выбирает её, а тянуть — двигает; колесо мыши меняет масштаб [viewport], зажатое колесо двигает холст.
 * Обведённая обрезка ждёт галочки; правая кнопка, клик мимо и Esc её отменяют.
 * Ctrl+Z / Ctrl+Y отменяют и повторяют, Delete удаляет выбранную правку, Enter подтверждает обрезку.
 */
@Composable
internal fun EditorCanvas(
    image: ImageBitmap,
    state: ScreenshotEditorState,
    onEvent: (ScreenshotEditorEvent) -> Unit,
    measurer: TextMeasurer,
    viewport: CanvasViewport,
    modifier: Modifier = Modifier,
) {
    val painter = remember(image, measurer) { EditPainter(image, measurer) }
    var area by remember { mutableStateOf(IntSize.Zero) }
    // масштаб заводится заново после обрезки, а размер области от этого не меняется — задаём его при каждом показе
    viewport.size = Size(area.width.toFloat(), area.height.toFloat())
    val fit = ImageFit.of(IntSize(image.width, image.height), state.history.orientation, state.bounds, area)
    val currentFit by rememberUpdatedState(fit)
    val currentState by rememberUpdatedState(state)
    val currentOnEvent by rememberUpdatedState(onEvent)
    val focus = remember { FocusRequester() }
    val colors = canvasColors()
    Box(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { area = it }
            .focusRequester(focus)
            .focusable()
            .onPreviewKeyEvent { it.handleEditorKeys(currentOnEvent) }
            .middleButtonPan(viewport)
            .wheelZoom(viewport),
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerHoverIcon(PointerIcon.Crosshair)
                .pointerInput(viewport) {
                    // правка начинается точно там, где нажали: detectDragGestures начинает её после порога сдвига
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        // окно поверх игры тянется за любое место: правка на холсте его двигать не должна
                        down.consume()
                        focus.requestFocus()
                        if (currentEvent.buttons.isSecondaryPressed) {
                            currentOnEvent(ScreenshotEditorEvent.Cancel)
                            return@awaitEachGesture
                        }
                        val toImage = { point: Offset -> currentFit.toImage(viewport.toContent(point)) }
                        val at = toImage(down.position)
                        val tolerance = ScreenshotEditorDimens.hitTolerance.toPx() / (currentFit.scale * viewport.zoom)
                        currentOnEvent(ScreenshotEditorEvent.Press(at, hitIndex(currentState.history.edits, at, tolerance, measurer)))
                        var moved = false
                        drag(down.id) { change ->
                            moved = true
                            change.consume()
                            currentOnEvent(ScreenshotEditorEvent.DragMove(toImage(change.position)))
                        }
                        currentOnEvent(ScreenshotEditorEvent.DragEnd)
                        if (!moved) currentOnEvent(ScreenshotEditorEvent.Tap(at))
                    }
                },
        ) {
            paintCanvas(CanvasScene(image, state, painter, fit, viewport, measurer), colors)
        }
        CanvasControls(state, fit, viewport, measurer, onEvent)
    }
}
