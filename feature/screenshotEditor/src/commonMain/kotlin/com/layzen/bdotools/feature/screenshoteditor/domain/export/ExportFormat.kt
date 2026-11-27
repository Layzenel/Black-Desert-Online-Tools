package com.layzen.bdotools.feature.screenshoteditor.domain.export

/**
 * Формат сохранённой картинки. PNG — без потерь, но тяжёлый (скриншот 4K — 5–10 МБ); JPEG — в разы легче,
 * влезает в лимит Discord. [extension] — расширение файла.
 */
enum class ExportFormat(val extension: String) {
    Png("png"),
    Jpeg("jpg"),
}
