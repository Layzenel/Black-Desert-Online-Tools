package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.core.folder.GameFolderSettings
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private const val FACES = "FaceTexture"
private const val BACKUP = "FaceTexture_backup"
private const val TOOLS = "BDO Tools"
private const val TOOL_PORTRAITS = "portraits"
private const val FAVORITES = "Favorite FaceTexture"
private const val GAME_FOLDER = "Black Desert"

/**
 * Файлы игры на диске через `java.nio.file` для настольной версии. Папки, имена копий и форматы списков те же,
 * что у моста File System Access API в браузере, поэтому [PortraitsRepository] работает одинаково.
 *
 * Папка выбирается системным окном [pickFolder] и запоминается в [settings]. Разрешений, как в браузере,
 * не бывает: если сохранённая папка пропала или в ней больше нет `FaceTexture`, доступ — [FolderAccess.NotSelected].
 */
internal class DesktopPortraitsStorage(
    private val settings: GameFolderSettings,
    private val pickFolder: suspend () -> Path?,
) : PortraitsStorage {

    override suspend fun access(): FolderAccess = fileCall {
        val folder = settings.folder?.takeIf(::isGameFolder)
        if (folder == null) FolderAccess.NotSelected else FolderAccess.Granted(folder.displayName())
    }

    override suspend fun selectFolder(): FolderAccess.Granted {
        val picked = pickFolder() ?: throw PortraitsException(PortraitsError.Cancelled)
        return fileCall {
            val root = gameFolder(picked) ?: throw PortraitsException(PortraitsError.NotGameFolder)
            settings.folder = root
            FolderAccess.Granted(root.displayName())
        }
    }

    override suspend fun requestPermission(): FolderAccess.Granted =
        access() as? FolderAccess.Granted ?: throw PortraitsException(PortraitsError.NotGameFolder)

    override suspend fun portraitIds(): List<String> = fileCall {
        fileNames(root() / FACES).filter { it.isBmpName() }.map { it.dropLast(PORTRAIT_EXTENSION.length) }
    }

    override suspend fun readPortrait(characterId: String): ByteArray = fileCall {
        (root() / FACES / portraitFile(characterId)).readBytes()
    }


    override suspend fun backupPortraits(characterIds: List<String>): String = fileCall {
        val root = root()
        val backups = (root / BACKUP).createDirectories()
        val folder = freshFolderName(backups)
        val target = (backups / folder).createDirectories()
        characterIds.map(::portraitFile).forEach { name -> (root / FACES / name).copyTo(target / name) }
        "$BACKUP\\$folder"
    }

    override suspend fun writePortrait(characterId: String, bmp: ByteArray) = fileCall {
        (root() / FACES / portraitFile(characterId)).writeBytes(bmp)
    }

    override suspend fun backupListing(): String = fileCall { folderListing(root() / BACKUP) }

    override suspend fun readBackupPortrait(backup: String, characterId: String): ByteArray = fileCall {
        (root() / BACKUP / backup / portraitFile(characterId)).readBytes()
    }

    override suspend fun deleteBackup(backup: String) = fileCall { deleteFolder(root() / BACKUP / backup) }

    override suspend fun toolFileNames(): List<String> = fileCall { fileNames(toolFolder()) }

    override suspend fun readToolFile(name: String): ByteArray? = fileCall { readOrNull(toolFolder() / name) }

    override suspend fun writeToolFile(name: String, bytes: ByteArray) = fileCall {
        (toolFolder().createDirectories() / name).writeBytes(bytes)
    }

    override suspend fun deleteToolFile(name: String) = fileCall {
        (toolFolder() / name).deleteIfExists()
        Unit
    }

    override suspend fun createFavoriteSet(): String = fileCall {
        val favorites = (root() / FAVORITES).createDirectories()
        val folder = freshFolderName(favorites)
        (favorites / folder).createDirectories()
        folder
    }

    override suspend fun favoriteListing(): String = fileCall { folderListing(root() / FAVORITES) }

    override suspend fun readFavoriteFile(set: String, name: String): ByteArray? = fileCall {
        readOrNull(root() / FAVORITES / set / name)
    }

    override suspend fun writeFavoriteFile(set: String, name: String, bytes: ByteArray) = fileCall {
        ((root() / FAVORITES / set).createDirectories() / name).writeBytes(bytes)
    }

    override suspend fun deleteFavoriteSet(set: String) = fileCall { deleteFolder(root() / FAVORITES / set) }

    /** Сохранённая папка `Black Desert`; без неё работать с файлами нельзя. */
    private fun root(): Path =
        settings.folder?.takeIf(::isGameFolder) ?: throw PortraitsException(PortraitsError.NotGameFolder)

    /** `Black Desert\BDO Tools\portraits` — чистые портреты и украшения. */
    private fun toolFolder(): Path = root() / TOOLS / TOOL_PORTRAITS

    private fun portraitFile(characterId: String) = characterId + PORTRAIT_EXTENSION

    private fun isGameFolder(folder: Path): Boolean = (folder / FACES).isDirectory()

    /** Подходит сама папка игры или папка, в которой лежит `Black Desert` (например, «Документы»). */
    private fun gameFolder(picked: Path): Path? = when {
        isGameFolder(picked) -> picked
        isGameFolder(picked / GAME_FOLDER) -> picked / GAME_FOLDER
        else -> null
    }

    private fun Path.displayName(): String = fileName?.name ?: toString()
}
