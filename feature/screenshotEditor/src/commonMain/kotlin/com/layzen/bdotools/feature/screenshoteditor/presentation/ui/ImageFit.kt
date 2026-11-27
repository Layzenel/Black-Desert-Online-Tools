package com.layzen.bdotools.feature.screenshoteditor.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImagePoint
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Orientation
import kotlin.math.min

/**
 * Как картинка [image] с поворотом [orientation] вписана в область экрана: масштаб [scale] и левый верхний угол
 * [offset] повёрнутой картинки в пикселях области. Картинка целиком видна и стоит по центру.
 */
internal data class ImageFit(val image: IntSize, val orientation: Orientation, val scale: Float, val offset: Offset) {
    /** Точка области экрана [point] в пикселях исходной картинки. */
    fun toImage(point: Offset): ImagePoint = orientation.toImage(
        ImagePoint((point.x - offset.x) / scale, (point.y - offset.y) / scale),
        image.width.toFloat(),
        image.height.toFloat(),
    )

    companion object {
        /** Картинка [image], повёрнутая [orientation], в области [area]. */
        fun of(image: IntSize, orientation: Orientation, area: IntSize): ImageFit {
            val (width, height) = orientation.size(image.width.toFloat(), image.height.toFloat())
            if (width == 0f || height == 0f || area.width == 0 || area.height == 0) return ImageFit(image, orientation, 1f, Offset.Zero)
            val scale = min(area.width / width, area.height / height)
            return ImageFit(image, orientation, scale, Offset((area.width - width * scale) / 2, (area.height - height * scale) / 2))
        }
    }
}
