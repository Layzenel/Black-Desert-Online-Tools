package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import kotlin.math.min

/**
 * Как картинка вписана в область экрана: масштаб [scale] и левый верхний угол [offset] в пикселях области.
 * Картинка целиком видна и стоит по центру.
 */
internal data class ImageFit(val scale: Float, val offset: Offset) {
    /** Точка области экрана [point] в пикселях картинки. */
    fun toImage(point: Offset) = ImagePoint((point.x - offset.x) / scale, (point.y - offset.y) / scale)

    companion object {
        /** Картинка [image] в области [area]. */
        fun of(image: IntSize, area: IntSize): ImageFit {
            if (image.width == 0 || image.height == 0 || area.width == 0 || area.height == 0) return ImageFit(1f, Offset.Zero)
            val scale = min(area.width.toFloat() / image.width, area.height.toFloat() / image.height)
            return ImageFit(scale, Offset((area.width - image.width * scale) / 2, (area.height - image.height * scale) / 2))
        }
    }
}
