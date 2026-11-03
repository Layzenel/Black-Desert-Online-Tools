package com.layzen.bdotools.feature.portraits.presentation.render

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraits.domain.ImagePlacement

/** Переводит систему координат из пикселей маски в пиксели изображения. Тот же порядок, что и в предпросмотре. */
internal fun Canvas.applyPlacement(placement: ImagePlacement, image: ImageBitmap) {
    translate(placement.centerX, placement.centerY)
    rotate(placement.rotationDegrees)
    scale(placement.scale, placement.scale)
    translate(-image.width / 2f, -image.height / 2f)
}
