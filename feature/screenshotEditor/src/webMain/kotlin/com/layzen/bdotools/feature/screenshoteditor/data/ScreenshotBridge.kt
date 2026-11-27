package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.core.folder.FolderBridge
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException

/** Код ответа моста, когда окно выбора закрыли: это не ошибка. */
internal const val CANCELLED = "CANCELLED"

/**
 * Мост редактора: папку игры узнаёт по `ScreenShot`. Выбор файла, скачивание и буфер обмена работают и без папки;
 * ошибки — [ScreenshotException] с кодом моста в сообщении.
 */
internal val screenshotBridge = FolderBridge(
    name = "__bdoScreenshots",
    requiredFolder = "ScreenShot",
    script = SCREENSHOT_BRIDGE_SOURCE,
    failure = { code ->
        val error = when (code) {
            "CLIPBOARD" -> ScreenshotError.Clipboard
            else -> ScreenshotError.Folder
        }
        ScreenshotException(error, code)
    },
)
