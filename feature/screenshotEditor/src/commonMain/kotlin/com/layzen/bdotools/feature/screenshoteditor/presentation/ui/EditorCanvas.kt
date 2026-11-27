package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.composable.input.handleHistoryKeys
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.presentation.event.ScreenshotEditorEvent
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.EditPainter
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.ScreenshotEditorState
import com.layzen.bdotools.uikit.theme.AppTheme

// Затемнение за рамкой обрезки: то, что не попадёт в копию, видно, но приглушено
private const val CROP_SHADE_ALPHA = 0.7f

/**
 * Скриншот с правками, вписанный в область: инструменты рисуют мышью, Ctrl+Z / Ctrl+Y отменяют и повторяют.
 * Обрезка показывается рамкой с затемнением вокруг — картинка остаётся целиком, чтобы рамку можно было переделать.
 */
@Composable
internal fun EditorCanvas(
    image: ImageBitmap,
    state: ScreenshotEditorState,
    onEvent: (ScreenshotEditorEvent) -> Unit,
    measurer: TextMeasurer,
    modifier: Modifier = Modifier,
) {
    val painter = remember(image, measurer) { EditPainter(image, measurer) }
    var area by remember { mutableStateOf(IntSize.Zero) }
    val fit = ImageFit.of(IntSize(image.width, image.height), area)
    val currentFit by rememberUpdatedState(fit)
    val currentOnEvent by rememberUpdatedState(onEvent)
    val focus = remember { FocusRequester() }
    val shade = AppTheme.colors.backgroundDeep.copy(alpha = CROP_SHADE_ALPHA)
    val border = AppTheme.colors.accentGold
    Canvas(
        modifier
            .fillMaxSize()
            .onSizeChanged { area = it }
            .focusRequester(focus)
            .focusable()
            .onPreviewKeyEvent { it.handleHistoryKeys({ currentOnEvent(ScreenshotEditorEvent.Undo) }, { currentOnEvent(ScreenshotEditorEvent.Redo) }) }
            .pointerHoverIcon(PointerIcon.Crosshair)
            .pointerInput(Unit) {
                // правка начинается точно там, где нажали: detectDragGestures начинает её после порога сдвига
                awaitEachGesture {
                    val down = awaitFirstDown()
                    focus.requestFocus()
                    currentOnEvent(ScreenshotEditorEvent.DragStart(currentFit.toImage(down.position)))
                    var moved = false
                    drag(down.id) { change ->
                        moved = true
                        change.consume()
                        currentOnEvent(ScreenshotEditorEvent.DragMove(currentFit.toImage(change.position)))
                    }
                    currentOnEvent(ScreenshotEditorEvent.DragEnd)
                    if (!moved) currentOnEvent(ScreenshotEditorEvent.Tap(currentFit.toImage(down.position)))
                }
            },
    ) {
        withTransform({
            translate(fit.offset.x, fit.offset.y)
            scale(fit.scale, fit.scale, Offset.Zero)
        }) {
            with(painter) {
                paint(state.history.drawn)
                state.draft?.let { paintEdit(it) }
            }
            val crop = (state.draft as? Edit.Crop)?.rect ?: state.history.crop
            if (crop != null) cropShade(crop, image, shade, border, ScreenshotEditorDimens.cropBorder.toPx() / fit.scale)
        }
    }
}

/** Затемняет картинку [image] вокруг обрезки [crop] и обводит её рамкой толщиной [borderWidth] пикселей картинки. */
private fun DrawScope.cropShade(
    crop: ImageRect,
    image: ImageBitmap,
    shade: Color,
    border: Color,
    borderWidth: Float,
) {
    val width = image.width.toFloat()
    val height = image.height.toFloat()
    drawRect(shade, Offset.Zero, Size(width, crop.top))
    drawRect(shade, Offset(0f, crop.bottom), Size(width, height - crop.bottom))
    drawRect(shade, Offset(0f, crop.top), Size(crop.left, crop.height))
    drawRect(shade, Offset(crop.right, crop.top), Size(width - crop.right, crop.height))
    drawRect(border, Offset(crop.left, crop.top), Size(crop.width, crop.height), style = Stroke(borderWidth))
}
