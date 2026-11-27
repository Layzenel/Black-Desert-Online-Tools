package com.layzen.bdotools.feature.screenshoteditor.presentation.gallery

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.presentation.state.EditorNotice

/**
 * Галерея скриншотов игры.
 *
 * @property access доступ к папке игры; `null` — ещё не проверили
 * @property entries скриншоты, новые первыми
 * @property thumbnails уменьшенные копии по [ScreenshotEntry.id]; грузятся, когда плитка попала на экран
 * @property canManageFiles можно показать файл в проводнике и перенести в корзину (настольная версия)
 */
data class GalleryState(
    val access: FolderAccess? = null,
    val entries: List<ScreenshotEntry> = emptyList(),
    val thumbnails: Map<String, ImageBitmap> = emptyMap(),
    val canManageFiles: Boolean = false,
    val notice: EditorNotice? = null,
)
