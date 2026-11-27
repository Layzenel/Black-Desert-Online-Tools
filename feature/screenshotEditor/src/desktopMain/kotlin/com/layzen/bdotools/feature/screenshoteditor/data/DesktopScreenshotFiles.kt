package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.screenshoteditor.domain.file.Screenshot
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotFiles
import com.layzen.bdotools.feature.screenshoteditor.domain.file.freeName
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import java.io.IOException
import java.nio.file.Path
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val imageExtensions = listOf("png", "jpg", "jpeg", "bmp", "webp")
private const val SCREENSHOT_FOLDER = "ScreenShot"
private const val TOOLS = "BDO Tools"
private const val MODULE = "screenshotEditor"

/**
 * Скриншоты на диске. Открывает их системным окном в `Black Desert\ScreenShot`, копии сохраняет в
 * `Black Desert\BDO Tools\screenshotEditor\` (без папки аккаунта: папка скриншотов в игре одна на все аккаунты),
 * а если папки игры нет — в `Документы\BDO Tools\screenshotEditor\`.
 *
 * @property gameFolder папка `Black Desert` или `null`, если её не нашли
 */
internal class DesktopScreenshotFiles(private val gameFolder: () -> Path?) : ScreenshotFiles {

    override suspend fun open(): Screenshot? {
        val start = gameFolder()?.resolve(SCREENSHOT_FOLDER)?.takeIf { it.isDirectory() } ?: documents()
        val file = FileKit.openFilePicker(type = FileKitType.File(imageExtensions), directory = PlatformFile(start.toFile()))
            ?.file?.toPath() ?: return null
        return withContext(Dispatchers.IO) { Screenshot(file.fileName.toString(), file.readBytes()) }
    }

    override suspend fun save(name: String, bytes: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            val folder = (gameFolder() ?: documents()).resolve(TOOLS).resolve(MODULE).createDirectories()
            val file = folder.resolve(freeName(name) { folder.resolve(it).exists() })
            file.writeBytes(bytes)
            file.toString()
        } catch (e: IOException) {
            throw ScreenshotException(ScreenshotError.Save, e.message)
        }
    }

    override suspend fun copy(png: ByteArray) = withContext(Dispatchers.IO) { writeClipboardImage(png) }

    private fun documents(): Path = FileSystemView.getFileSystemView().defaultDirectory.toPath()
}

internal actual fun createScreenshotFiles(): ScreenshotFiles = DesktopScreenshotFiles { GameFolderSettings().folder }
