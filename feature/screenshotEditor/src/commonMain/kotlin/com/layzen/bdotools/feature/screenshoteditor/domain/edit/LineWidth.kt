package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.math.max

// Толщина 1 — это пиксель на каждые 1000 пикселей длинной стороны: на 4K-скриншоте линия та же на вид, что на Full HD
private const val PIXELS_PER_STEP = 1000f
private const val LABEL_SIZE_PER_WIDTH = 6f

/** Толщина линии в пикселях картинки [imageWidth]×[imageHeight] для толщины [thickness] из настроек. */
fun lineWidth(thickness: Int, imageWidth: Int, imageHeight: Int): Float =
    thickness * max(1f, max(imageWidth, imageHeight) / PIXELS_PER_STEP)

/** Высота букв надписи в пикселях картинки для толщины [thickness]: в 6 раз больше толщины линии. */
fun labelSize(thickness: Int, imageWidth: Int, imageHeight: Int): Float =
    lineWidth(thickness, imageWidth, imageHeight) * LABEL_SIZE_PER_WIDTH

private const val COUNTER_RADIUS_PER_WIDTH = 3.5f

/** Радиус кружка с номером в пикселях картинки для толщины [thickness]. */
fun counterRadius(thickness: Int, imageWidth: Int, imageHeight: Int): Float =
    lineWidth(thickness, imageWidth, imageHeight) * COUNTER_RADIUS_PER_WIDTH
