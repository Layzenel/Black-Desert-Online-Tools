package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.core.folder.FolderAccess

/** Хранилище в памяти для тестов: файлы игры, папки BDO Tools и избранного — обычные Map, [log] — порядок записей. */
internal class MemoryStorage : PortraitsStorage {
    val faces = mutableMapOf<String, ByteArray>()
    val tools = mutableMapOf<String, ByteArray>()
    /** Избранное: ключ — `<папка>/<файл>`. */
    val favorites = mutableMapOf<String, ByteArray>()
    val log = mutableListOf<String>()

    override suspend fun access(): FolderAccess = FolderAccess.Granted("Black Desert")
    override suspend fun selectFolder() = FolderAccess.Granted("Black Desert")
    override suspend fun requestPermission() = FolderAccess.Granted("Black Desert")
    override suspend fun portraitIds() = faces.keys.toList()
    override suspend fun readPortrait(characterId: String) = faces.getValue(characterId)
    override suspend fun characterOrderCandidates() = emptyList<String>()

    override suspend fun backupPortraits(characterIds: List<String>): String {
        log += "backup:${characterIds.joinToString()}"
        return "FaceTexture_backup\\test"
    }

    override suspend fun writePortrait(characterId: String, bmp: ByteArray) {
        log += "face:$characterId"
        faces[characterId] = bmp
    }

    val backups = mutableMapOf<String, List<String>>()

    override suspend fun backupListing() =
        backups.entries.joinToString("\n") { (name, ids) -> name + "\t" + ids.joinToString(",") { "$it.bmp" } }

    override suspend fun deleteBackup(backup: String) {
        backups.remove(backup)
    }
    override suspend fun readBackupPortrait(backup: String, characterId: String) = ByteArray(0)
    override suspend fun toolFileNames() = tools.keys.toList()
    override suspend fun readToolFile(name: String) = tools[name]
    override suspend fun writeToolFile(name: String, bytes: ByteArray) {
        tools[name] = bytes
    }

    override suspend fun deleteToolFile(name: String) {
        tools.remove(name)
    }

    override suspend fun createFavoriteSet(): String = "set${favorites.keys.map { it.substringBefore('/') }.toSet().size + 1}"

    override suspend fun favoriteListing() =
        favorites.keys.groupBy({ it.substringBefore('/') }, { it.substringAfter('/') })
            .entries.joinToString("\n") { (set, files) -> set + "\t" + files.joinToString(",") }

    override suspend fun readFavoriteFile(set: String, name: String) = favorites["$set/$name"]
    override suspend fun writeFavoriteFile(set: String, name: String, bytes: ByteArray) {
        favorites["$set/$name"] = bytes
    }

    override suspend fun deleteFavoriteSet(set: String) {
        favorites.keys.removeAll { it.startsWith("$set/") }
    }
}
