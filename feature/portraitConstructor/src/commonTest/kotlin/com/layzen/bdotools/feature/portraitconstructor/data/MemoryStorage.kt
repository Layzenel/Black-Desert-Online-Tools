package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess

/**
 * Хранилище в памяти для тестов: файлы игры, папки BDO Tools, копий и избранного — обычные Map, [log] — порядок записей.
 * В копиях и избранном ключ — `<папка>/<файл>`, а папка аккаунта — `<аккаунт>:<папка>`; без аккаунта — просто `<папка>`.
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

    override suspend fun createBackup(account: String?): String {
        // имена как у настоящих копий, чтобы порядок «новые первыми» совпадал с порядком создания
        val name = "2026-11-05_10-00-${(++createdBackups).toString().padStart(2, '0')}"
        log += "backup:${scoped(account, name)}"
        return name
    }

    override suspend fun writePortrait(characterId: String, bmp: ByteArray) {
        log += "face:$characterId"
        faces[characterId] = bmp
    }

    override suspend fun backupListing(account: String?) = listing(backups, account)
    override suspend fun deleteBackup(account: String?, backup: String) {
        backups.keys.removeAll { it.startsWith("${scoped(account, backup)}/") }
    }

    override suspend fun readBackupFile(account: String?, backup: String, name: String) = backups["${scoped(account, backup)}/$name"]
    override suspend fun writeBackupFile(account: String?, backup: String, name: String, bytes: ByteArray) {
        val key = "${scoped(account, backup)}/$name"
        log += "backupFile:$key"
        backups[key] = bytes
    }

    override suspend fun toolFileNames() = tools.keys.toList()
    override suspend fun readToolFile(name: String) = tools[name]
    override suspend fun writeToolFile(name: String, bytes: ByteArray) {
        tools[name] = bytes
    }

    override suspend fun deleteToolFile(name: String) {
        tools.remove(name)
    }

    override suspend fun createFavoriteSet(account: String?): String =
        "set${favorites.keys.map { it.substringBefore('/') }.toSet().size + 1}"

    override suspend fun favoriteListing(account: String?) = listing(favorites, account)
    override suspend fun readFavoriteFile(account: String?, set: String, name: String) = favorites["${scoped(account, set)}/$name"]
    override suspend fun writeFavoriteFile(account: String?, set: String, name: String, bytes: ByteArray) {
        favorites["${scoped(account, set)}/$name"] = bytes
    }

    override suspend fun deleteFavoriteSet(account: String?, set: String) {
        favorites.keys.removeAll { it.startsWith("${scoped(account, set)}/") }
    }

    override suspend fun adoptLooseFolders(account: String) {
        listOf(backups, favorites).forEach { files ->
            files.keys.filter { ':' !in it.substringBefore('/') }.forEach { key -> files["$account:$key"] = files.remove(key)!! }
        }
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

    private fun scoped(account: String?, folder: String) = account?.let { "$it:$folder" } ?: folder

    /** Папки аккаунта [account] (без аккаунта — папки без префикса) строками `<папка>\t<файлы>`. */
    private fun listing(files: Map<String, ByteArray>, account: String?) =
        files.keys
            .map { it.substringBefore('/') to it.substringAfter('/') }
            .filter { (folder, _) -> if (account == null) ':' !in folder else folder.startsWith("$account:") }
            .groupBy({ it.first.substringAfter(':') }, { it.second })
            .entries.joinToString("\n") { (folder, names) -> folder + "\t" + names.joinToString(",") }
}
