package com.layzen.bdotools.core.account

import com.layzen.bdotools.core.folder.GameFolderSettings
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val CACHE = "UserCache"
private const val FACES = "FaceTexture"
private const val GAME_VARIABLE_FILE = "gamevariable.xml"

/** Аккаунты на диске через `java.nio.file`; [folder] — выбранная папка `Black Desert` или `null`. */
internal class DesktopAccountFiles(private val folder: () -> Path?) : AccountFiles {

    override suspend fun accountFiles(): List<AccountFile> = withContext(Dispatchers.IO) {
        val cache = folder()?.let { it / CACHE }?.takeIf { it.isDirectory() } ?: return@withContext emptyList()
        cache.listDirectoryEntries().filter { it.isDirectory() }.flatMap { account ->
            account.listDirectoryEntries()
                .filter { it.isRegularFile() && it.name.equals(GAME_VARIABLE_FILE, ignoreCase = true) }
                .map { AccountFile(account.name, it.getLastModifiedTime().toMillis(), it.readText()) }
        }
    }

    override suspend fun readPortrait(characterId: String): ByteArray? = withContext(Dispatchers.IO) {
        folder()?.let { it / FACES / "$characterId.bmp" }?.takeIf { it.isRegularFile() }?.readBytes()
    }
}

internal actual fun createAccountFiles(): AccountFiles = GameFolderSettings().let { DesktopAccountFiles { it.folder } }
