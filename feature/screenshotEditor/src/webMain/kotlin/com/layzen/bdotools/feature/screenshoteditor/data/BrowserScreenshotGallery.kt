package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.screenshoteditor.domain.file.Screenshot
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotGallery
import kotlin.io.encoding.Base64

/**
 * Папка `ScreenShot` в браузере через File System Access API (только Chromium). Папка `Black Desert` общая с
 * другими тулзами сайта: выбранная там видна и здесь. Показать в проводнике и удалить файл браузер не умеет —
 * [canManageFiles] выключен, а [reveal] и [moveToTrash] бросают [ScreenshotException].
 */
internal class BrowserScreenshotGallery : ScreenshotGallery {
    override val canManageFiles = false

    override suspend fun access(): FolderAccess = screenshotBridge.access()

    override suspend fun selectFolder(): FolderAccess =
        try {
            screenshotBridge.selectFolder()
        } catch (e: ScreenshotException) {
            if (e.message == CANCELLED) access() else throw e
        }

    override suspend fun requestPermission(): FolderAccess = screenshotBridge.requestPermission()

    override suspend fun list(): List<ScreenshotEntry> =
        screenshotBridge.call("list").lines().filter { it.isNotBlank() }.map { line ->
            val name = line.substringBefore('\t')
            ScreenshotEntry(name, name, line.substringAfter('\t').toLongOrNull() ?: 0)
        }

    override suspend fun read(entry: ScreenshotEntry): Screenshot =
        Screenshot(entry.name, Base64.decode(screenshotBridge.call("read", entry.id)))

    override suspend fun thumbnail(entry: ScreenshotEntry): ByteArray = Base64.decode(screenshotBridge.call("thumb", entry.id))

    override suspend fun reveal(entry: ScreenshotEntry) = throw ScreenshotException(ScreenshotError.Folder, "browser cannot reveal files")

    override suspend fun moveToTrash(entry: ScreenshotEntry) = throw ScreenshotException(ScreenshotError.Trash, "browser cannot trash files")
}
