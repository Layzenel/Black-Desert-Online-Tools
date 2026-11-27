package com.layzen.bdotools.feature.screenshoteditor.data

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.max

/** Ширина миниатюры в галерее: плитка шире не бывает, а больше пикселей — больше памяти на сотни скриншотов. */
private const val THUMBNAIL_WIDTH = 360

/**
 * Уменьшенная копия картинки [file] в PNG. Читает не весь файл, а каждый N-й пиксель (subsampling ImageIO):
 * скриншот 4K целиком — 30 МБ памяти и полсекунды на плитку.
 */
internal fun thumbnailPng(file: Path): ByteArray {
    val input = ImageIO.createImageInputStream(file.toFile()) ?: throw IOException("cannot open $file")
    input.use {
        val reader = ImageIO.getImageReaders(input).asSequence().firstOrNull() ?: throw IOException("not an image: $file")
        try {
            reader.input = input
            // с округлением вверх: миниатюра не шире THUMBNAIL_WIDTH
            val step = max(1, (reader.getWidth(0) + THUMBNAIL_WIDTH - 1) / THUMBNAIL_WIDTH)
            val param = reader.defaultReadParam.apply { setSourceSubsampling(step, step, 0, 0) }
            val image = reader.read(0, param)
            return ByteArrayOutputStream().use { out ->
                ImageIO.write(image, "png", out)
                out.toByteArray()
            }
        } finally {
            reader.dispose()
        }
    }
}
