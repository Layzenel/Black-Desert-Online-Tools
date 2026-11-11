package com.layzen.bdotools.feature.uieditor.presentation.ui

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize

/**
 * Картинка, уменьшенная до высоты [height] px с теми же пропорциями. Портрет из `FaceTexture` — 624×804:
 * целиком для каждого персонажа он занимал бы в памяти по 2 МБ, а в списке нужна маленькая копия.
 */
internal fun ImageBitmap.thumbnail(height: Int = SlotsDimens.PORTRAIT_THUMBNAIL_HEIGHT): ImageBitmap {
    if (this.height <= height) return this
    val size = IntSize((width * height / this.height).coerceAtLeast(1), height)
    val result = ImageBitmap(size.width, size.height)
    Canvas(result).drawImageRect(this, dstSize = size, paint = Paint().apply { filterQuality = FilterQuality.Medium })
    return result
}
