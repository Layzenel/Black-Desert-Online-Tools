package com.layzen.bdotools.feature.screenshoteditor.domain.file

import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat

/** Сценарии работы с файлами скриншотов: открыть, сохранить копию, скопировать в буфер. */
class ScreenshotInteractor(private val files: ScreenshotFiles) {
    /** См. [ScreenshotFiles.open]. */
    suspend fun open(): Screenshot? = files.open()

    /** Сохраняет картинку [bytes] копией скриншота [source] в формате [format]; оригинал не трогает. См. [ScreenshotFiles.save]. */
    suspend fun saveCopy(source: String, format: ExportFormat, bytes: ByteArray): String? =
        files.save(editedName(source, format), bytes)

    /** См. [ScreenshotFiles.copy]. */
    suspend fun copy(png: ByteArray) = files.copy(png)
}
