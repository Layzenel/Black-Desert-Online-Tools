package com.layzen.bdotools.feature.screenshoteditor.domain.file

/**
 * Открытие скриншота и выдача результата: на Desktop — файлы на диске, на сайте — выбор файла и скачивание.
 * Ошибки — [ScreenshotException].
 */
interface ScreenshotFiles {
    /** Выбор скриншота; окно открывается в папке `ScreenShot`, если она есть. `null` — окно закрыли. */
    suspend fun open(): Screenshot?

    /**
     * Сохраняет картинку [bytes] под именем [name], не перезаписывая существующие файлы.
     * Возвращает путь к файлу или `null`, если файл ушёл в загрузки браузера.
     */
    suspend fun save(name: String, bytes: ByteArray): String?

    /** Кладёт PNG [png] в буфер обмена, чтобы сразу вставить в Discord или чат. */
    suspend fun copy(png: ByteArray)
}
