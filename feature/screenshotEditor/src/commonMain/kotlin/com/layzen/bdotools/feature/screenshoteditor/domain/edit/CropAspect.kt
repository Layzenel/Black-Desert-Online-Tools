package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.math.abs
import kotlin.math.sign

/**
 * Пропорции обрезки. [ratio] — ширина к высоте, `null` — свободно.
 * Квадрат и вертикаль — для аватарок и историй, 16:9 — для обоев и видео.
 */
enum class CropAspect(val ratio: Float?) {
    Free(null),
    Wide(16f / 9f),
    Square(1f),
    Tall(9f / 16f);

    /**
     * Рамка обрезки, которую тянут из [from] в [to]: ширина — по движению мыши, высота — по пропорции,
     * в ту сторону, куда тянут. У картинки, повёрнутой набок ([sideways]), пропорция берётся обратная: рамку
     * видно повёрнутой.
     */
    fun rect(from: ImagePoint, to: ImagePoint, sideways: Boolean = false): ImageRect {
        val ratio = ratio?.let { if (sideways) 1 / it else it } ?: return ImageRect.between(from, to)
        val dx = to.x - from.x
        val dy = to.y - from.y
        val width = maxOf(abs(dx), abs(dy) * ratio)
        val height = width / ratio
        // тянут строго по горизонтали или вертикали — вниз и вправо
        val signX = if (dx == 0f) 1f else dx.sign
        val signY = if (dy == 0f) 1f else dy.sign
        return ImageRect.between(from, ImagePoint(from.x + width * signX, from.y + height * signY))
    }
}
