package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess

/**
 * Хранилище в памяти для тестов: файлы игры, папки BDO Tools, копий и избранного — обычные Map, [log] — порядок записей.
 * В копиях и избранном ключ — `<папка>/<файл>`.
 */
internal class MemoryStorage : PortraitsStorage {
    val faces = mutableMapOf<String, ByteArray>()
    val tools = mutableMapOf<String, ByteArray>()
    val backups = mutableMapOf<String, ByteArray>()
    val favorites = mutableMapOf<String, ByteArray>()
    val log = mutableListOf<String>()
    private var createdBackups = 0

    override suspend fun access(): FolderAccess = FolderAccess.Granted("Black Desert")
    override suspend fun selectFolder() = FolderAccess.Granted("Black Desert")
    override suspend fun requestPermission() = FolderAccess.Granted("Black Desert")
    override suspend fun portraitIds() = faces.keys.toList()
    override suspend fun readPortrait(characterId: String) = faces.getValue(characterId)

    override suspend fun createBackup(): String {
        // имена как у настоящих копий, чтобы порядок «новые первыми» совпадал с порядком создания
        val name = "2026-11-05_10-00-${(++createdBackups).toString().padStart(2, '0')}"
        log += "backup:$name"
        return name
    }

    override suspend fun writePortrait(characterId: String, bmp: ByteArray) {
        log += "face:$characterId"
        faces[characterId] = bmp
    }

    override suspend fun backupListing() = listing(backups)
    override suspend fun deleteBackup(backup: String) {
        backups.keys.removeAll { it.startsWith("$backup/") }
    }

    override suspend fun readBackupFile(backup: String, name: String) = backups["$backup/$name"]
    override suspend fun writeBackupFile(backup: String, name: String, bytes: ByteArray) {
        log += "backupFile:$backup/$name"
        backups["$backup/$name"] = bytes
    }

    override suspend fun toolFileNames() = tools.keys.toList()
    override suspend fun readToolFile(name: String) = tools[name]
    override suspend fun writeToolFile(name: String, bytes: ByteArray) {
        tools[name] = bytes
    }

    override suspend fun deleteToolFile(name: String) {
        tools.remove(name)
    }

    override suspend fun createFavoriteSet(): String = "set${favorites.keys.map { it.substringBefore('/') }.toSet().size + 1}"
    override suspend fun favoriteListing() = listing(favorites)
    override suspend fun readFavoriteFile(set: String, name: String) = favorites["$set/$name"]
    override suspend fun writeFavoriteFile(set: String, name: String, bytes: ByteArray) {
        favorites["$set/$name"] = bytes
    }

    override suspend fun deleteFavoriteSet(set: String) {
        favorites.keys.removeAll { it.startsWith("$set/") }
    }

    /** `gameVariable_lobby.xml` по аккаунтам и копии: `<аккаунт>/<имя>`. */
    val lobby = mutableMapOf<String, ByteArray>()
    val lobbyBackups = mutableMapOf<String, ByteArray>()

    override suspend fun readLobbyVariables(account: String) = lobby[account]
    override suspend fun writeLobbyVariables(account: String, bytes: ByteArray) {
        lobby[account] = bytes
    }

    override suspend fun backupLobbyVariables(account: String, bytes: ByteArray): String {
        val path = "$account/${lobbyBackups.size + 1}.xml"
        lobbyBackups[path] = bytes
        return path
    }

    var gameRunning: Boolean? = null

    override suspend fun isGameRunning() = gameRunning

    private fun listing(files: Map<String, ByteArray>) =
        files.keys.groupBy({ it.substringBefore('/') }, { it.substringAfter('/') })
            .entries.joinToString("\n") { (folder, names) -> folder + "\t" + names.joinToString(",") }
}
