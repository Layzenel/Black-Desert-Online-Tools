package com.layzen.bdotools.feature.screenshoteditor.domain.file

import com.layzen.bdotools.core.folder.FolderAccess

/**
 * Скриншоты в папке `Документы\Black Desert\ScreenShot`. На Desktop папка находится сама, в браузере её дают
 * через File System Access API (только Chromium). Ошибки — [ScreenshotException].
 */
interface ScreenshotGallery {
    /** Можно ли перенести скриншот в корзину и показать в проводнике: только в настольной версии. */
    val canManageFiles: Boolean

    /** Есть ли доступ к папке игры. */
    suspend fun access(): FolderAccess

    /** Выбор папки `Black Desert` (в браузере). */
    suspend fun selectFolder(): FolderAccess

    /** Повторный запрос разрешения на выбранную раньше папку (в браузере). */
    suspend fun requestPermission(): FolderAccess

    /** Скриншоты игры в папке, новые первыми. */
    suspend fun list(): List<ScreenshotEntry>

    /** Сам скриншот [entry]. */
    suspend fun read(entry: ScreenshotEntry): Screenshot

    /** Уменьшенная копия [entry] для сетки (PNG или JPEG): полный скриншот на каждую плитку — сотни мегабайт. */
    suspend fun thumbnail(entry: ScreenshotEntry): ByteArray

    /** Открывает проводник с выделенным [entry]. */
    suspend fun reveal(entry: ScreenshotEntry)

    /** Переносит [entry] в корзину Windows — его можно вернуть оттуда. */
    suspend fun moveToTrash(entry: ScreenshotEntry)
}
