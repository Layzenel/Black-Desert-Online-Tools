package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.feature.screenshoteditor.domain.file.Screenshot
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotFiles
import kotlin.io.encoding.Base64

/**
 * Скриншоты в браузере: выбор файла через `<input type="file">`, сохранение — скачиванием (в папку загрузок
 * браузера), копирование — Clipboard API. Работает в любом браузере, File System Access API не нужен.
 */
internal class BrowserScreenshotFiles : ScreenshotFiles {
    override suspend fun open(): Screenshot? {
        val picked = try {
            screenshotBridge.call("pickImage")
        } catch (e: ScreenshotException) {
            if (e.message == CANCELLED) return null else throw e
        }
        return Screenshot(picked.substringBefore('\n'), Base64.decode(picked.substringAfter('\n')))
    }

    override suspend fun save(name: String, bytes: ByteArray): String? {
        screenshotBridge.call("save", name, Base64.encode(bytes))
        return null
    }

    override suspend fun copy(png: ByteArray) {
        screenshotBridge.call("copyImage", Base64.encode(png))
    }
}
