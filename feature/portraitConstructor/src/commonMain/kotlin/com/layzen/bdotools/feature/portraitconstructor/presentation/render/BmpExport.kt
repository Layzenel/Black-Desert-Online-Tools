package com.layzen.bdotools.feature.portraitconstructor.presentation.render

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.encodeBmp

/** Пиксели изображения в формате .bmp из `FaceTexture`. */
internal fun ImageBitmap.toBmp(): ByteArray {
    val pixels = IntArray(width * height)
    readPixels(pixels)
    return encodeBmp(width, height, pixels)
}
