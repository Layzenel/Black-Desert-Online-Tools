package com.layzen.bdotools.feature.portraits.data

/** Хранилище в памяти для тестов: файлы игры и папка BDO Tools — обычные Map, [log] — порядок записей. */
internal class MemoryStorage : PortraitsStorage {
    val faces = mutableMapOf<String, ByteArray>()
    val tools = mutableMapOf<String, ByteArray>()
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
}
