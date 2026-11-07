package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.portraitconstructor.domain.parseBackupList
import java.nio.file.Files
import java.nio.file.Path
import java.util.prefs.Preferences
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalPathApi::class)
class DesktopPortraitsStorageTest {
    private val documents: Path = Files.createTempDirectory("bdo-tools-test")
    private val game = (documents / "Black Desert").also { (it / "FaceTexture").createDirectories() }
    private val preferences = Preferences.userRoot().node("com/layzen/bdotools/test/${System.nanoTime()}")
    private var picked: Path? = documents
    private val storage = DesktopPortraitsStorage(GameFolderSettings(preferences)) { picked }

    @AfterTest
    fun cleanUp() {
        preferences.removeNode()
        documents.deleteRecursively()
    }

    @Test
    fun accessIsRememberedAndLostWithFolder() = runTest {
        assertEquals(FolderAccess.NotSelected, storage.access())
        assertEquals(FolderAccess.Granted("Black Desert"), storage.selectFolder())
        assertEquals(FolderAccess.Granted("Black Desert"), storage.access())
        game.deleteRecursively()
        assertEquals(FolderAccess.NotSelected, storage.access())
    }

    @Test
    fun folderWithoutGameIsRejected() = runTest {
        picked = (documents / "Other").createDirectories()
        assertEquals(PortraitsError.NotGameFolder, assertFailsWith<PortraitsException> { storage.selectFolder() }.error)
        picked = null
        assertEquals(PortraitsError.Cancelled, assertFailsWith<PortraitsException> { storage.selectFolder() }.error)
    }

    @Test
    fun backupKeepsFilesAndIsListed() = runTest {
        storage.selectFolder()
        (game / "FaceTexture" / "101.bmp").writeBytes(byteArrayOf(1))
        (game / "FaceTexture" / "102.BMP").writeBytes(byteArrayOf(2))
        (game / "FaceTexture" / "notes.txt").writeText("x")
        assertEquals(setOf("101", "102"), storage.portraitIds().toSet())

        val first = storage.createBackup(ACCOUNT)
        val second = storage.createBackup(ACCOUNT)
        assertNotEquals(first, second)
        storage.writeBackupFile(ACCOUNT, first, "101.bmp", byteArrayOf(1))
        storage.writeBackupFile(ACCOUNT, first, "101.txt", byteArrayOf(3))
        storage.writeBackupFile(ACCOUNT, second, "101.bmp", byteArrayOf(1))

        val backups = parseBackupList(storage.backupListing(ACCOUNT))
        assertEquals(2, backups.size)
        assertEquals(setOf("101"), backups.first().characterIds)
        assertContentEquals(byteArrayOf(3), storage.readBackupFile(ACCOUNT, first, "101.txt"))
        assertNull(storage.readBackupFile(ACCOUNT, second, "101.txt"))

        storage.deleteBackup(ACCOUNT, backups.first().name)
        assertEquals(1, parseBackupList(storage.backupListing(ACCOUNT)).size)
    }

    @Test
    fun toolAndFavoriteFiles() = runTest {
        storage.selectFolder()
        assertEquals(emptyList(), storage.toolFileNames())
        assertNull(storage.readToolFile("101.txt"))
        storage.writeToolFile("101.txt", byteArrayOf(7))
        assertContentEquals(byteArrayOf(7), (game / "BDO Tools" / "portraits" / "101.txt").readBytes())
        storage.deleteToolFile("101.txt")
        storage.deleteToolFile("101.txt")
        assertEquals(emptyList(), storage.toolFileNames())

        val set = storage.createFavoriteSet(ACCOUNT)
        storage.writeFavoriteFile(ACCOUNT, set, "101.bmp", byteArrayOf(3))
        storage.writeFavoriteFile(ACCOUNT, set, "name.txt", byteArrayOf(4))
        assertEquals("$set\t101.bmp", storage.favoriteListing(ACCOUNT))
        assertContentEquals(byteArrayOf(4), storage.readFavoriteFile(ACCOUNT, set, "name.txt"))
        storage.deleteFavoriteSet(ACCOUNT, set)
        assertFalse((game / "Favorite FaceTexture" / ACCOUNT / set).exists())
    }

    @Test
    fun backupsLiveInAccountFolderAndLooseOnesMoveThere() = runTest {
        storage.selectFolder()
        val loose = (game / "FaceTexture_backup" / "2026-11-01_10-00-00").createDirectories()
        (loose / "101.bmp").writeBytes(byteArrayOf(1))
        (game / "Favorite FaceTexture" / "2026-11-02_10-00-00").createDirectories()
        (game / "FaceTexture_backup" / "222").createDirectories()

        storage.adoptLooseFolders(ACCOUNT)

        assertFalse(loose.exists())
        assertEquals("2026-11-01_10-00-00\t101.bmp", storage.backupListing(ACCOUNT))
        assertEquals("2026-11-02_10-00-00\t", storage.favoriteListing(ACCOUNT))
        // папка другого аккаунта осталась на месте
        assertTrue((game / "FaceTexture_backup" / "222").exists())

        val created = storage.createBackup(ACCOUNT)
        assertTrue((game / "FaceTexture_backup" / ACCOUNT / created).exists())
    }

    private companion object {
        const val ACCOUNT = "111"
    }
}
