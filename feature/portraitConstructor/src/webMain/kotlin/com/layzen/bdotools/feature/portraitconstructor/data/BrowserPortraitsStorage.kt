package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsError
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsException
import kotlin.io.encoding.Base64

private const val BMP_EXTENSION = ".bmp"

/** Доступ к файлам игры через File System Access API (Chrome, Edge, Opera, Яндекс.Браузер). */
internal class BrowserPortraitsStorage : GameFiles {

    override suspend fun access(): FolderAccess = fileSystemBridge.access()

    override suspend fun selectFolder() = fileSystemBridge.selectFolder()

    override suspend fun requestPermission() = fileSystemBridge.requestPermission()

    override suspend fun portraitIds(): List<String> =
        fileSystemBridge.call("list")
            .split('\n')
            .filter { it.endsWith(BMP_EXTENSION, ignoreCase = true) }
            .map { it.dropLast(BMP_EXTENSION.length) }

    override suspend fun readPortrait(characterId: String): ByteArray =
        Base64.decode(fileSystemBridge.call("read", characterId + BMP_EXTENSION))

    override suspend fun createBackup(account: String?): String = fileSystemBridge.call("backupCreate", account.orEmpty())

    override suspend fun writePortrait(characterId: String, bmp: ByteArray) {
        fileSystemBridge.call("write", characterId + BMP_EXTENSION, Base64.encode(bmp))
    }

    override suspend fun backupListing(account: String?): String = fileSystemBridge.call("backups", account.orEmpty())

    override suspend fun readBackupFile(account: String?, backup: String, name: String): ByteArray? {
        val result = fileSystemBridge.call("backupRead", "${account.orEmpty()}/$backup", name)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeBackupFile(account: String?, backup: String, name: String, bytes: ByteArray) {
        fileSystemBridge.call("backupWrite", "${account.orEmpty()}/$backup/$name", Base64.encode(bytes))
    }

    override suspend fun deleteBackup(account: String?, backup: String) {
        fileSystemBridge.call("backupDelete", account.orEmpty(), backup)
    }

    override suspend fun toolFileNames(account: String?): List<String> =
        fileSystemBridge.call("toolList", account.orEmpty()).split('\n').filter { it.isNotEmpty() }

    override suspend fun readToolFile(account: String?, name: String): ByteArray? {
        val result = fileSystemBridge.call("toolRead", account.orEmpty(), name)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeToolFile(account: String?, name: String, bytes: ByteArray) {
        fileSystemBridge.call("toolWrite", "${account.orEmpty()}/$name", Base64.encode(bytes))
    }

    override suspend fun deleteToolFile(account: String?, name: String) {
        fileSystemBridge.call("toolDelete", account.orEmpty(), name)
    }

    override suspend fun createFavoriteSet(account: String?): String = fileSystemBridge.call("favoriteCreate", account.orEmpty())

    override suspend fun favoriteListing(account: String?): String = fileSystemBridge.call("favorites", account.orEmpty())

    override suspend fun readFavoriteFile(account: String?, set: String, name: String): ByteArray? {
        val result = fileSystemBridge.call("favoriteRead", "${account.orEmpty()}/$set", name)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeFavoriteFile(account: String?, set: String, name: String, bytes: ByteArray) {
        fileSystemBridge.call("favoriteWrite", "${account.orEmpty()}/$set/$name", Base64.encode(bytes))
    }

    override suspend fun deleteFavoriteSet(account: String?, set: String) {
        fileSystemBridge.call("favoriteDelete", account.orEmpty(), set)
    }

    override suspend fun adoptLooseFolders(account: String, characterIds: Set<String>) {
        fileSystemBridge.call("adoptLoose", account, characterIds.joinToString("\n"))
    }

    override suspend fun readLobbyVariables(account: String): ByteArray? {
        val result = fileSystemBridge.call("lobbyRead", account)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeLobbyVariables(account: String, bytes: ByteArray) {
        fileSystemBridge.call("lobbyWrite", account, Base64.encode(bytes))
    }

    override suspend fun backupLobbyVariables(account: String, bytes: ByteArray): String =
        fileSystemBridge.call("lobbyBackup", account, Base64.encode(bytes))

    /** Браузер не видит процессы, поэтому закрыта ли игра, подтверждает пользователь. */
    override suspend fun isGameRunning(): Boolean? = null

    override suspend fun pickFromDevice(): ByteArray? =
        try {
            Base64.decode(fileSystemBridge.call("pickImage"))
        } catch (e: PortraitsException) {
            if (e.error == PortraitsError.Cancelled) null else throw e
        }

    override suspend fun download(url: String): ByteArray = Base64.decode(fileSystemBridge.call("download", url))

    override suspend fun pasteFromClipboard(): ByteArray = Base64.decode(fileSystemBridge.call("pasteImage"))
}

internal actual fun createGameFiles(): GameFiles = BrowserPortraitsStorage()
