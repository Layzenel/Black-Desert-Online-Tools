package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.screenshoteditor.domain.file.Screenshot
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotException
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotGallery
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import java.awt.Desktop
import java.io.IOException
import java.nio.file.Path
import javax.swing.filechooser.FileSystemView
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Папка скриншотов игры внутри `Black Desert`. */
internal const val SCREENSHOT_FOLDER = "ScreenShot"
private const val GAME_FOLDER = "Black Desert"

/**
 * Папка `ScreenShot` на диске. Папку `Black Desert` берёт из общих настроек ([gameFolder]); выбранную вручную
 * сохраняет туда же ([saveGameFolder]) — её увидят и другие тулзы.
 */
internal class DesktopScreenshotGallery(
    private val gameFolder: () -> Path?,
    private val saveGameFolder: (Path) -> Unit,
) : ScreenshotGallery {
    override val canManageFiles = true

    override suspend fun access(): FolderAccess = withContext(Dispatchers.IO) {
        val game = gameFolder()
        if (game != null && game.resolve(SCREENSHOT_FOLDER).isDirectory()) FolderAccess.Granted(game.name) else FolderAccess.NotSelected
    }

    override suspend fun selectFolder(): FolderAccess {
        val documents = PlatformFile(FileSystemView.getFileSystemView().defaultDirectory)
        val picked = FileKit.openDirectoryPicker(directory = documents)?.file?.toPath() ?: return access()
        // подходит сама папка Black Desert или папка, в которой она лежит (например, «Документы»)
        val game = listOf(picked, picked.resolve(GAME_FOLDER)).firstOrNull { it.resolve(SCREENSHOT_FOLDER).isDirectory() }
            ?: throw ScreenshotException(ScreenshotError.Folder, "no $SCREENSHOT_FOLDER in $picked")
        saveGameFolder(game)
        return access()
    }

    override suspend fun requestPermission(): FolderAccess = access()

    override suspend fun list(): List<ScreenshotEntry> = io {
        val folder = gameFolder()?.resolve(SCREENSHOT_FOLDER) ?: return@io emptyList()
        folder.listDirectoryEntries().filter { it.isRegularFile() }
            .map { ScreenshotEntry(it.toString(), it.name, it.getLastModifiedTime().toMillis()) }
    }

    override suspend fun read(entry: ScreenshotEntry): Screenshot = io { Screenshot(entry.name, Path.of(entry.id).readBytes()) }

    override suspend fun thumbnail(entry: ScreenshotEntry): ByteArray = io { thumbnailPng(Path.of(entry.id)) }

    override suspend fun reveal(entry: ScreenshotEntry) {
        io { ProcessBuilder("explorer.exe", "/select,${entry.id}").start() }
    }

    override suspend fun moveToTrash(entry: ScreenshotEntry) = withContext(Dispatchers.IO) {
        val desktop = Desktop.getDesktop().takeIf { Desktop.isDesktopSupported() && it.isSupported(Desktop.Action.MOVE_TO_TRASH) }
        if (desktop?.moveToTrash(Path.of(entry.id).toFile()) != true) throw ScreenshotException(ScreenshotError.Trash, entry.id)
    }

    /** [block] в потоке ввода-вывода; ошибка диска — [ScreenshotError.Folder]. */
    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: IOException) {
            throw ScreenshotException(ScreenshotError.Folder, e.message)
        }
    }
}
