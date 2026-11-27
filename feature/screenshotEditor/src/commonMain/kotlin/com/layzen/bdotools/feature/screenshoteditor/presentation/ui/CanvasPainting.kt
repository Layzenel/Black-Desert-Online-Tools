package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import com.layzen.bdotools.feature.screenshoteditor.domain.adjust.Adjustments
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Edit
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.presentation.controller.bounds
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.bounds
import com.layzen.bdotools.feature.screenshoteditor.presentation.render.oriented

/** Холст целиком: видимая часть скриншота с правками, пунктир вокруг выбранной правки и затемнение вокруг обрезки. */
internal fun DrawScope.paintCanvas(scene: CanvasScene, colors: CanvasColors) {
    val fit = scene.fit
    withTransform({
        translate(scene.viewport.pan.x, scene.viewport.pan.y)
        scale(scene.viewport.zoom, scene.viewport.zoom, Offset.Zero)
    }) {
        clipRect(fit.visible.left, fit.visible.top, fit.visible.right, fit.visible.bottom) {
            withTransform({
                translate(fit.offset.x, fit.offset.y)
                scale(fit.scale, fit.scale, Offset.Zero)
            }) {
                oriented(fit.orientation, scene.image.width.toFloat(), scene.image.height.toFloat()) {
                    paintImage(scene)
                    if (!scene.state.comparing) paintMarks(scene, colors)
                }
            }
        }
    }
}

/** Скриншот с ретушью и правками; в режиме сравнения — исходный, без них. */
private fun DrawScope.paintImage(scene: CanvasScene) {
    val state = scene.state
    with(scene.painter) {
        if (state.comparing) {
            paint(emptyList(), Adjustments(), state.bounds)
            return
        }
        paint(state.shownDrawn, state.shownAdjustments, state.bounds)
        state.draft?.takeUnless { it is Edit.Crop }?.let { paintEdit(it) }
    }
}

/** Пунктир вокруг выбранной правки и обводка обрезки — её тянут или она ждёт галочки. */
private fun DrawScope.paintMarks(scene: CanvasScene, colors: CanvasColors) {
    val line = ScreenshotEditorDimens.cropBorder.toPx() / scene.pixelsPerImagePixel
    scene.state.shownSelection?.bounds(scene.measurer)?.let { selection ->
        val dash = PathEffect.dashPathEffect(
            floatArrayOf(
                ScreenshotEditorDimens.selectionDash.toPx() / scene.pixelsPerImagePixel,
                ScreenshotEditorDimens.selectionGap.toPx() / scene.pixelsPerImagePixel,
            ),
        )
        drawRect(colors.outline, selection.topLeft, selection.size, style = Stroke(line * 2))
        drawRect(colors.border, selection.topLeft, selection.size, style = Stroke(line, pathEffect = dash))
    }
    val crop = (scene.state.draft as? Edit.Crop)?.rect ?: scene.state.shownCrop ?: return
    cropShade(crop, scene.state.bounds, colors, line)
}

/** Затемняет видимую часть [visible] вокруг обрезки [crop] и обводит её рамкой толщиной [line] пикселей картинки. */
private fun DrawScope.cropShade(crop: ImageRect, visible: ImageRect, colors: CanvasColors, line: Float) {
    // одной фигурой с дыркой: четыре соседних прямоугольника оставляли светлые швы на стыках
    val shade = Path().apply {
        fillType = PathFillType.EvenOdd
        addRect(Rect(visible.left, visible.top, visible.right, visible.bottom))
        addRect(Rect(crop.left, crop.top, crop.right, crop.bottom))
    }
    drawPath(shade, colors.shade)
    drawRect(colors.border, crop.topLeft, crop.size, style = Stroke(line))
}

private val ImageRect.topLeft get() = Offset(left, top)
private val ImageRect.size get() = Size(width, height)
