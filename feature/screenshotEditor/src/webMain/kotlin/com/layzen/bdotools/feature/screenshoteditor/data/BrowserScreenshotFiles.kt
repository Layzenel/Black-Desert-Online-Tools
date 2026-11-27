package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.core.folder.FolderBridge
import com.layzen.bdotools.feature.screenshoteditor.domain.file.Screenshot
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotFiles
import kotlin.io.encoding.Base64

private const val CANCELLED = "CANCELLED"

/** Мост редактора: папка игры ему не нужна — файл выбирается окном браузера, копия уходит в загрузки. */
private val bridge = FolderBridge(
    name = "__bdoScreenshots",
    requiredFolder = "ScreenShot",
    script = SCREENSHOT_BRIDGE_SOURCE,
    failure = { code ->
        ScreenshotException(if (code == "CLIPBOARD") ScreenshotError.Clipboard else ScreenshotError.Save, code)
    },
)

/**
 * Скриншоты в браузере: выбор файла через `<input type="file">`, сохранение — скачиванием (в папку загрузок
 * браузера), копирование — Clipboard API. Работает в любом браузере, File System Access API не нужен.
 */
internal class BrowserScreenshotFiles : ScreenshotFiles {
    override suspend fun open(): Screenshot? {
        val picked = try {
            bridge.call("pickImage")
        } catch (e: ScreenshotException) {
            if (e.message == CANCELLED) return null else throw e
        }
        return Screenshot(picked.substringBefore('\n'), Base64.decode(picked.substringAfter('\n')))
    }

    override suspend fun save(name: String, bytes: ByteArray): String? {
        bridge.call("save", name, Base64.encode(bytes))
        return null
    }

    override suspend fun copy(png: ByteArray) {
        bridge.call("copyImage", Base64.encode(png))
    }
}

internal actual fun createScreenshotFiles(): ScreenshotFiles = BrowserScreenshotFiles()
