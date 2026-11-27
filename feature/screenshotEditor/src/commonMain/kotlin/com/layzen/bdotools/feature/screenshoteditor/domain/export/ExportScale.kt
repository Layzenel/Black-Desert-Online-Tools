package com.layzen.bdotools.feature.screenshoteditor.domain.export

import kotlin.math.max
import kotlin.math.roundToInt

/** Размер сохранённой картинки относительно обрезанной: [percent] процентов по каждой стороне. */
enum class ExportScale(val percent: Int) {
    Full(100),
    ThreeQuarters(75),
    Half(50),
    Quarter(25);

    /** Размер картинки [width]×[height] после уменьшения, не меньше пикселя по стороне. */
    fun applyTo(width: Int, height: Int) = PixelSize(
        max(1, (width * percent / 100f).roundToInt()),
        max(1, (height * percent / 100f).roundToInt()),
    )
}
