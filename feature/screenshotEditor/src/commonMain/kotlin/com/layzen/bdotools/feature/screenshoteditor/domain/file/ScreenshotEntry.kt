package com.layzen.bdotools.feature.screenshoteditor.domain.file

/**
 * Скриншот в папке `ScreenShot`: [id] — как его найти (путь на Desktop, имя файла в браузере), [name] — имя файла,
 * [modified] — время изменения в миллисекундах (по нему галерея ставит новые первыми).
 */
data class ScreenshotEntry(val id: String, val name: String, val modified: Long = 0)
