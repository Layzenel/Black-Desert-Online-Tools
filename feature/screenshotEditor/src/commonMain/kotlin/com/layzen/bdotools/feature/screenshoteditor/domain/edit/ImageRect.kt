package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Прямоугольник на скриншоте в пикселях исходной картинки; [left] ≤ [right], [top] ≤ [bottom]. */
data class ImageRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    /** Ширина в пикселях. */
    val width: Float get() = right - left

    /** Высота в пикселях. */
    val height: Float get() = bottom - top

    /** Меньше пикселя по одной из сторон: такую область правкой не считаем (случайный клик). */
    val isEmpty: Boolean get() = width < 1f || height < 1f

    /** Точка [point] внутри прямоугольника. */
    operator fun contains(point: ImagePoint): Boolean = point.x in left..right && point.y in top..bottom

    /** Тот же прямоугольник по целым пикселям внутри картинки [imageWidth]×[imageHeight]. */
    fun snappedInto(imageWidth: Int, imageHeight: Int): ImageRect = ImageRect(
        left.roundToInt().coerceIn(0, imageWidth).toFloat(),
        top.roundToInt().coerceIn(0, imageHeight).toFloat(),
        right.roundToInt().coerceIn(0, imageWidth).toFloat(),
        bottom.roundToInt().coerceIn(0, imageHeight).toFloat(),
    )

    companion object {
        /** Прямоугольник между двумя углами в любом порядке: так тянут рамку мышью в любую сторону. */
        fun between(a: ImagePoint, b: ImagePoint) = ImageRect(min(a.x, b.x), min(a.y, b.y), max(a.x, b.x), max(a.y, b.y))
    }
}
