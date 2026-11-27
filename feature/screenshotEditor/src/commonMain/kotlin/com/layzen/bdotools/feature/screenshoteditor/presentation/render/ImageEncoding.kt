package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

private const val JPEG_QUALITY = 92

/** Картинка [image] байтами файла [format] (через Skia — она есть и в браузере, и на Desktop). */
internal fun encode(image: ImageBitmap, format: ExportFormat): ByteArray {
    val skiaFormat = when (format) {
        ExportFormat.Png -> EncodedImageFormat.PNG
        ExportFormat.Jpeg -> EncodedImageFormat.JPEG
    }
    val data = Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(skiaFormat, JPEG_QUALITY)
    return checkNotNull(data) { "Skia could not encode $format" }.bytes
}
