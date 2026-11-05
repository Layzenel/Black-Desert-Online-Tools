package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.core.folder.FolderAccess
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

    override suspend fun createBackup(): String = fileSystemBridge.call("backupCreate")

    override suspend fun writePortrait(characterId: String, bmp: ByteArray) {
        fileSystemBridge.call("write", characterId + BMP_EXTENSION, Base64.encode(bmp))
    }

    override suspend fun backupListing(): String = fileSystemBridge.call("backups")

    override suspend fun readBackupFile(backup: String, name: String): ByteArray? {
        val result = fileSystemBridge.call("backupRead", backup, name)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeBackupFile(backup: String, name: String, bytes: ByteArray) {
        fileSystemBridge.call("backupWrite", "$backup/$name", Base64.encode(bytes))
    }

    override suspend fun deleteBackup(backup: String) {
        fileSystemBridge.call("backupDelete", backup)
    }

    override suspend fun toolFileNames(): List<String> =
        fileSystemBridge.call("toolList").split('\n').filter { it.isNotEmpty() }

    override suspend fun readToolFile(name: String): ByteArray? {
        val result = fileSystemBridge.call("toolRead", name)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeToolFile(name: String, bytes: ByteArray) {
        fileSystemBridge.call("toolWrite", name, Base64.encode(bytes))
    }

    override suspend fun deleteToolFile(name: String) {
        fileSystemBridge.call("toolDelete", name)
    }

    override suspend fun createFavoriteSet(): String = fileSystemBridge.call("favoriteCreate")

    override suspend fun favoriteListing(): String = fileSystemBridge.call("favorites")

    override suspend fun readFavoriteFile(set: String, name: String): ByteArray? {
        val result = fileSystemBridge.call("favoriteRead", set, name)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeFavoriteFile(set: String, name: String, bytes: ByteArray) {
        fileSystemBridge.call("favoriteWrite", "$set/$name", Base64.encode(bytes))
    }

    override suspend fun deleteFavoriteSet(set: String) {
        fileSystemBridge.call("favoriteDelete", set)
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
}

internal actual fun createGameFiles(): GameFiles = BrowserPortraitsStorage()
