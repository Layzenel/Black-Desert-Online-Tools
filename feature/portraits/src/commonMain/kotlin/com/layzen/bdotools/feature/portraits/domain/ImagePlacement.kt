package com.layzen.bdotools.feature.portraits.domain

import kotlin.math.max

/**
 * Положение загруженного изображения под маской, в пикселях текстуры.
 * Центр изображения стоит в точке ([centerX], [centerY]), изображение увеличено в [scale] раз
 * и повёрнуто на [rotationDegrees] по часовой стрелке вокруг своего центра.
 */
data class ImagePlacement(
    val centerX: Float,
    val centerY: Float,
    val scale: Float,
    val rotationDegrees: Float,
) {
    fun movedBy(dx: Float, dy: Float) = copy(centerX = centerX + dx, centerY = centerY + dy)

    fun zoomedTo(newScale: Float) = copy(scale = newScale.coerceIn(MIN_SCALE, MAX_SCALE))

    fun rotatedTo(degrees: Float) = copy(rotationDegrees = degrees.coerceIn(-MAX_ROTATION, MAX_ROTATION))

    companion object {
        const val MIN_SCALE = 0.05f
        const val MAX_SCALE = 20f
        const val MAX_ROTATION = 180f

        /** Изображение по центру сетки, целиком закрывает её без поворота. */
        fun cover(imageWidth: Int, imageHeight: Int, grid: PortraitGrid): ImagePlacement {
            val scale = max(grid.width.toFloat() / imageWidth, grid.height.toFloat() / imageHeight)
            return ImagePlacement(
                centerX = grid.width / 2f,
                centerY = grid.height / 2f,
                scale = scale.coerceIn(MIN_SCALE, MAX_SCALE),
                rotationDegrees = 0f,
            )
        }
    }
}
