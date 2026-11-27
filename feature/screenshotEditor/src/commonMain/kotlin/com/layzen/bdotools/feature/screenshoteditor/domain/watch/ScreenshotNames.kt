package com.layzen.bdotools.feature.screenshoteditor.domain.watch

// Так называет файлы игра: `2026-11-27_230089871.PNG` — дата и число. Всё остальное в папке (копии, чужие файлы) — не скриншот
private val gameScreenshot = Regex("""^\d{4}-\d{2}-\d{2}_\d+\.(png|jpe?g|bmp)$""", RegexOption.IGNORE_CASE)

private val PNG_END = byteArrayOf(0x49, 0x45, 0x4E, 0x44, 0xAE.toByte(), 0x42, 0x60, 0x82.toByte())
private val JPEG_END = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

/** Имя файла [name] — скриншот, который сделала игра. */
fun isGameScreenshotName(name: String): Boolean = gameScreenshot.matches(name)

/**
 * Файл [name] дописан до конца: последние байты [tail] — конец PNG (блок `IEND`) или JPEG (`FF D9`).
 * У BMP конца нет — его готовность видна только по тому, что размер перестал расти.
 */
fun looksComplete(name: String, tail: ByteArray): Boolean = when (name.substringAfterLast('.').lowercase()) {
    "png" -> tail.endsWith(PNG_END)
    "jpg", "jpeg" -> tail.endsWith(JPEG_END)
    else -> true
}

/** Сколько байт с конца файла нужно для [looksComplete]. */
const val COMPLETE_TAIL_BYTES = 8

private fun ByteArray.endsWith(end: ByteArray) = size >= end.size && end.indices.all { this[size - end.size + it] == end[it] }
