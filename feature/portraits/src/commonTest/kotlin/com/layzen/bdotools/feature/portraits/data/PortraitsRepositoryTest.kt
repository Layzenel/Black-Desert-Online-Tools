package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

private class MemoryStorage : PortraitsStorage {
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

class PortraitsRepositoryTest {

    private val decorated = PortraitDecoration().withCaption("Рыбак").withBadge(BadgeSlot.Top, BadgeIcon.Fisher)

    @Test
    fun cleanBaseFallsBackToCurrentPortrait() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(1) }
        val repository = PortraitsRepository(storage)

        assertContentEquals(byteArrayOf(1), repository.readCleanBase("1"))

        storage.tools["1.bmp"] = byteArrayOf(9)
        assertContentEquals(byteArrayOf(9), repository.readCleanBase("1"))
    }

    @Test
    fun decoratedPortraitKeepsCleanBaseAndDecoration() = runTest {
        val storage = MemoryStorage().apply { faces["1"] = byteArrayOf(1) }
        val repository = PortraitsRepository(storage)

        val backup = repository.writePortraits(listOf(PortraitWrite("1", face = byteArrayOf(2), cleanBase = byteArrayOf(1), decoration = decorated)))

        assertEquals("FaceTexture_backup\\test", backup)
        assertEquals(listOf("backup:1", "face:1"), storage.log)
        assertContentEquals(byteArrayOf(2), storage.faces["1"])
        assertContentEquals(byteArrayOf(1), storage.tools["1.bmp"])
        assertEquals(mapOf("1" to decorated), repository.loadDecorations())
    }

    @Test
    fun emptyDecorationRemovesStoredFiles() = runTest {
        val storage = MemoryStorage().apply {
            faces["1"] = byteArrayOf(2)
            tools["1.bmp"] = byteArrayOf(1)
            tools["1.txt"] = "caption=Рыбак\n".encodeToByteArray()
        }
        val repository = PortraitsRepository(storage)

        repository.writePortraits(listOf(PortraitWrite("1", face = byteArrayOf(1), cleanBase = byteArrayOf(1), decoration = PortraitDecoration())))

        assertNull(storage.tools["1.bmp"])
        assertNull(storage.tools["1.txt"])
        assertContentEquals(byteArrayOf(1), storage.faces["1"])
        assertEquals(emptyMap(), repository.loadDecorations())
    }

    @Test
    fun deletesBackupsAndReturnsRest() = runTest {
        val storage = MemoryStorage().apply {
            backups["2026-11-01_10-00-00"] = listOf("1", "2")
            backups["2026-11-02_10-00-00"] = listOf("1")
            backups["2026-11-03_10-00-00"] = listOf("2")
        }
        val progress = mutableListOf<Int>()

        val left = PortraitsRepository(storage).deleteBackups(listOf("2026-11-01_10-00-00", "2026-11-03_10-00-00")) { done, _ -> progress += done }

        assertEquals(listOf("2026-11-02_10-00-00"), left.map { it.name })
        assertEquals(listOf(1, 2), progress)
    }
}
