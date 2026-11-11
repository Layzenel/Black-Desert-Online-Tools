package com.layzen.bdotools.feature.portraitconstructor.domain.portrait

private const val FILE_HEADER_SIZE = 14
private const val INFO_HEADER_SIZE = 40
private const val BYTES_PER_PIXEL = 4
private const val PIXELS_PER_METER = 2835 // 72 DPI, как в файлах игры

/**
 * Кодирует изображение в BMP того же вида, что лежит в `FaceTexture`:
 * 32 бита на пиксель, без сжатия (BI_RGB), строки снизу вверх, байты в порядке B, G, R, A.
 *
 * [argb] — пиксели построчно сверху вниз в формате `0xAARRGGBB`.
 */
fun encodeBmp(width: Int, height: Int, argb: IntArray): ByteArray {
    require(width > 0 && height > 0) { "Размер изображения должен быть больше нуля" }
    require(argb.size == width * height) { "Ожидалось ${width * height} пикселей, получено ${argb.size}" }

    val dataOffset = FILE_HEADER_SIZE + INFO_HEADER_SIZE
    val imageSize = width * height * BYTES_PER_PIXEL
    val bytes = ByteArray(dataOffset + imageSize)

    fun putShort(at: Int, value: Int) {
        bytes[at] = value.toByte()
        bytes[at + 1] = (value shr 8).toByte()
    }

    fun putInt(at: Int, value: Int) {
        putShort(at, value)
        putShort(at + 2, value shr 16)
    }

    // BITMAPFILEHEADER
    bytes[0] = 'B'.code.toByte()
    bytes[1] = 'M'.code.toByte()
    putInt(2, bytes.size)
    putInt(10, dataOffset)

    // BITMAPINFOHEADER
    putInt(14, INFO_HEADER_SIZE)
    putInt(18, width)
    putInt(22, height)
    putShort(26, 1)
    putShort(28, BYTES_PER_PIXEL * 8)
    putInt(30, 0)
    putInt(34, 0)
    putInt(38, PIXELS_PER_METER)
    putInt(42, PIXELS_PER_METER)

    var at = dataOffset
    for (row in height - 1 downTo 0) {
        val rowStart = row * width
        for (column in 0 until width) {
            val pixel = argb[rowStart + column]
            bytes[at] = pixel.toByte()
            bytes[at + 1] = (pixel shr 8).toByte()
            bytes[at + 2] = (pixel shr 16).toByte()
            bytes[at + 3] = (pixel ushr 24).toByte()
            at += BYTES_PER_PIXEL
        }
    }
    return bytes
}
