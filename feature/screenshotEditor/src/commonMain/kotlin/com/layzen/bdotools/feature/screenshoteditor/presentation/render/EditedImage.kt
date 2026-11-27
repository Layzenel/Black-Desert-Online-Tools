package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale
import kotlin.math.roundToInt

/**
 * Готовая картинка для сохранения: [source] с правками [history] в полном разрешении, обрезанная и уменьшенная
 * до [scale]. Надписи меряет [measurer].
 */
internal fun renderEdited(source: ImageBitmap, history: EditHistory, scale: ExportScale, measurer: TextMeasurer): ImageBitmap {
    val crop = history.crop ?: ImageRect(0f, 0f, source.width.toFloat(), source.height.toFloat())
    val width = crop.width.roundToInt()
    val height = crop.height.roundToInt()
    val full = ImageBitmap(width, height)
    val painter = EditPainter(source, measurer)
    // плотность 1: размер надписей в правках задан в пикселях картинки
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(full), Size(width.toFloat(), height.toFloat())) {
        translate(-crop.left, -crop.top) {
            with(painter) { paint(history.drawn) }
        }
    }
    if (scale == ExportScale.Full) return full
    val size = scale.applyTo(width, height)
    val scaled = ImageBitmap(size.width, size.height)
    Canvas(scaled).drawImageRect(
        full,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(width, height),
        dstSize = IntSize(size.width, size.height),
        paint = Paint().apply { filterQuality = FilterQuality.High },
    )
    return scaled
}
