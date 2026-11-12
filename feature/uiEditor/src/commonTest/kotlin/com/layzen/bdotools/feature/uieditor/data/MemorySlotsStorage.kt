package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.feature.uieditor.domain.game.GameFile

/** Хранилище в памяти для тестов репозитория: один аккаунт, его `gamevariable.xml` и файлы персонажей. */
class MemorySlotsStorage(var gameVariable: String, val characters: MutableMap<String, String>) : SlotsStorage {
    private var modified = 1L

    override suspend fun access(): FolderAccess = FolderAccess.Granted("Black Desert")
    override suspend fun selectFolder() = FolderAccess.Granted("Black Desert")
    override suspend fun requestPermission() = FolderAccess.Granted("Black Desert")
    override suspend fun accounts() = listOf(ACCOUNT)
    override suspend fun readGameVariable(account: String) = GameFile(gameVariable, modified)

    override suspend fun writeGameVariable(account: String, text: String, expectedModified: Long): String {
        check(expectedModified == modified) { "file changed" }
        gameVariable = text
        modified++
        return "backup"
    }

    override suspend fun characterFiles(account: String) = characters.keys.toList()
    override suspend fun readCharacterFile(account: String, path: String) = characters.getValue(path)
    override suspend fun writeCharacterFile(account: String, path: String, text: String) {
        characters[path] = text
    }

    override suspend fun presetFiles(account: String) = emptyList<String>()
    override suspend fun readPreset(account: String, fileName: String) = error("no presets")
    override suspend fun writePreset(account: String, fileName: String, text: String) = Unit
    override suspend fun deletePreset(account: String, fileName: String) = Unit
    override suspend fun readBackground(account: String): ByteArray? = null
    override suspend fun writeBackground(account: String, bytes: ByteArray?) = Unit
    override suspend fun adoptLooseFiles(account: String) = Unit

    companion object {
        const val ACCOUNT = "3537922"
    }
}
