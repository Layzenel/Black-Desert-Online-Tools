package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Orientation
import kotlin.math.min

/**
 * Как часть [frame] картинки [image] с поворотом [orientation] вписана в область экрана: масштаб [scale] и левый
 * верхний угол [offset] всей повёрнутой картинки в пикселях области. Часть [frame] целиком видна и стоит по центру,
 * а её место на экране — [visible].
 */
internal data class ImageFit(
    val image: IntSize,
    val orientation: Orientation,
    val scale: Float,
    val offset: Offset,
    val visible: Rect,
) {
    /** Точка области экрана [point] в пикселях исходной картинки. */
    fun toImage(point: Offset): ImagePoint = orientation.toImage(
        ImagePoint((point.x - offset.x) / scale, (point.y - offset.y) / scale),
        image.width.toFloat(),
        image.height.toFloat(),
    )

    /** Точка исходной картинки [point] в пикселях области экрана. */
    fun toScreen(point: ImagePoint): Offset {
        val shown = orientation.toDisplay(point, image.width.toFloat(), image.height.toFloat())
        return Offset(shown.x * scale + offset.x, shown.y * scale + offset.y)
    }

    /** Прямоугольник картинки [rect] на экране: после поворота углы меняются местами, берём охватывающий. */
    fun toScreen(rect: ImageRect): Rect = bounding(
        listOf(
            toScreen(ImagePoint(rect.left, rect.top)),
            toScreen(ImagePoint(rect.right, rect.top)),
            toScreen(ImagePoint(rect.left, rect.bottom)),
            toScreen(ImagePoint(rect.right, rect.bottom)),
        ),
    )

    companion object {
        /** Часть [frame] картинки [image], повёрнутой [orientation], в области [area]. */
        fun of(image: IntSize, orientation: Orientation, frame: ImageRect, area: IntSize): ImageFit {
            val unit = ImageFit(image, orientation, 1f, Offset.Zero, Rect.Zero)
            val shown = unit.toScreen(frame)
            if (shown.width == 0f || shown.height == 0f || area.width == 0 || area.height == 0) return unit
            val scale = min(area.width / shown.width, area.height / shown.height)
            val left = (area.width - shown.width * scale) / 2
            val top = (area.height - shown.height * scale) / 2
            val offset = Offset(left - shown.left * scale, top - shown.top * scale)
            return ImageFit(image, orientation, scale, offset, Rect(left, top, left + shown.width * scale, top + shown.height * scale))
        }

        private fun bounding(points: List<Offset>) = Rect(
            points.minOf { it.x },
            points.minOf { it.y },
            points.maxOf { it.x },
            points.maxOf { it.y },
        )
    }
}
