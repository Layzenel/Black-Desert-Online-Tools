package com.layzen.bdotools.feature.portraits.data

import kotlin.io.encoding.Base64

private const val BMP_EXTENSION = ".bmp"

/** Доступ к файлам игры через File System Access API (Chrome, Edge, Opera, Яндекс.Браузер). */
internal class BrowserPortraitsStorage : GameFiles {

    override suspend fun access(): FolderAccess {
        if (!FileSystemBridge.isSupported) return FolderAccess.Unsupported
        val status = FileSystemBridge.call("status")
        return when (status.substringBefore(':')) {
            "granted" -> FolderAccess.Granted(status.substringAfter(':'))
            "prompt" -> FolderAccess.NeedsPermission(status.substringAfter(':'))
            else -> FolderAccess.NotSelected
        }
    }

    override suspend fun selectFolder() = FolderAccess.Granted(FileSystemBridge.call("pick"))

    override suspend fun requestPermission() = FolderAccess.Granted(FileSystemBridge.call("request"))

    override suspend fun portraitIds(): List<String> =
        FileSystemBridge.call("list")
            .split('\n')
            .filter { it.endsWith(BMP_EXTENSION, ignoreCase = true) }
            .map { it.dropLast(BMP_EXTENSION.length) }

    override suspend fun readPortrait(characterId: String): ByteArray =
        Base64.decode(FileSystemBridge.call("read", characterId + BMP_EXTENSION))

    override suspend fun characterOrderCandidates(): List<String> =
        FileSystemBridge.call("order").split(FileSystemBridge.ORDER_SEPARATOR).filter { it.isNotBlank() }

    override suspend fun backupPortraits(characterIds: List<String>): String =
        FileSystemBridge.call("backup", characterIds.joinToString("\n") { it + BMP_EXTENSION })

    override suspend fun writePortrait(characterId: String, bmp: ByteArray) {
        FileSystemBridge.call("write", characterId + BMP_EXTENSION, Base64.encode(bmp))
    }

    override suspend fun backupListing(): String = FileSystemBridge.call("backups")

    override suspend fun readBackupPortrait(backup: String, characterId: String): ByteArray =
        Base64.decode(FileSystemBridge.call("backupRead", backup, characterId + BMP_EXTENSION))

    override suspend fun deleteBackup(backup: String) {
        FileSystemBridge.call("backupDelete", backup)
    }

    override suspend fun toolFileNames(): List<String> =
        FileSystemBridge.call("toolList").split('\n').filter { it.isNotEmpty() }

    override suspend fun readToolFile(name: String): ByteArray? {
        val result = FileSystemBridge.call("toolRead", name)
        return if (result.startsWith("1")) Base64.decode(result.substring(1)) else null
    }

    override suspend fun writeToolFile(name: String, bytes: ByteArray) {
        FileSystemBridge.call("toolWrite", name, Base64.encode(bytes))
    }

    override suspend fun deleteToolFile(name: String) {
        FileSystemBridge.call("toolDelete", name)
    }

    override suspend fun pickFromDevice(): ByteArray? =
        try {
            Base64.decode(FileSystemBridge.call("pickImage"))
        } catch (e: PortraitsException) {
            if (e.error == PortraitsError.Cancelled) null else throw e
        }

    override suspend fun download(url: String): ByteArray = Base64.decode(FileSystemBridge.call("download", url))
}

internal actual fun createGameFiles(): GameFiles = BrowserPortraitsStorage()
