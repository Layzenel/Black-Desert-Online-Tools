package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.EditHistory
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Orientation
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportScale
import kotlin.math.roundToInt

/**
 * Готовая картинка для сохранения: [source] с ретушью и правками [history] в полном разрешении, обрезанная,
 * уменьшенная до [scale] и повёрнутая. Надписи меряет [measurer].
 */
internal fun renderEdited(source: ImageBitmap, history: EditHistory, scale: ExportScale, measurer: TextMeasurer): ImageBitmap {
    val crop = history.crop ?: ImageRect(0f, 0f, source.width.toFloat(), source.height.toFloat())
    val width = crop.width.roundToInt()
    val height = crop.height.roundToInt()
    val painter = EditPainter(source, measurer)
    val full = draw(width, height) {
        translate(-crop.left, -crop.top) {
            with(painter) { paint(history.drawn, history.adjustments, crop) }
        }
    }
    val size = scale.applyTo(width, height)
    val scaled = if (scale == ExportScale.Full) full else ImageBitmap(size.width, size.height).also {
        Canvas(it).drawImageRect(
            full,
            srcSize = IntSize(width, height),
            dstSize = IntSize(size.width, size.height),
            paint = Paint().apply { filterQuality = FilterQuality.High },
        )
    }
    val orientation = history.orientation
    if (orientation == Orientation()) return scaled
    val (turnedWidth, turnedHeight) = orientation.size(size.width.toFloat(), size.height.toFloat())
    return draw(turnedWidth.roundToInt(), turnedHeight.roundToInt()) {
        oriented(orientation, size.width.toFloat(), size.height.toFloat()) { drawImage(scaled) }
    }
}

/** Новая картинка [width]×[height], нарисованная [block] с плотностью 1: размеры в правках — пиксели картинки. */
private fun draw(width: Int, height: Int, block: DrawScope.() -> Unit): ImageBitmap {
    val image = ImageBitmap(width, height)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(width.toFloat(), height.toFloat()), block)
    return image
}
