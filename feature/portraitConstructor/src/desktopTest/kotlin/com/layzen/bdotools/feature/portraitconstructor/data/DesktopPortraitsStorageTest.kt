package com.layzen.bdotools.feature.portraitconstructor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.parseBackupList
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsError
import com.layzen.bdotools.feature.portraitconstructor.domain.error.PortraitsException
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
        assertEquals(emptyList(), storage.toolFileNames(ACCOUNT))
        assertNull(storage.readToolFile(ACCOUNT, "101.txt"))
        storage.writeToolFile(ACCOUNT, "101.txt", byteArrayOf(7))
        assertContentEquals(byteArrayOf(7), (tool / ACCOUNT / "Current portraits" / "101.txt").readBytes())
        storage.deleteToolFile(ACCOUNT, "101.txt")
        storage.deleteToolFile(ACCOUNT, "101.txt")
        assertEquals(emptyList(), storage.toolFileNames(ACCOUNT))

        val set = storage.createFavoriteSet(ACCOUNT)
        storage.writeFavoriteFile(ACCOUNT, set, "101.bmp", byteArrayOf(3))
        storage.writeFavoriteFile(ACCOUNT, set, "name.txt", byteArrayOf(4))
        assertEquals("$set\t101.bmp", storage.favoriteListing(ACCOUNT))
        assertContentEquals(byteArrayOf(4), storage.readFavoriteFile(ACCOUNT, set, "name.txt"))
        storage.deleteFavoriteSet(ACCOUNT, set)
        assertFalse((tool / ACCOUNT / "Favorite FaceTexture" / set).exists())
    }

    @Test
    fun legacyFoldersMoveIntoAccountFolders() = runTest {
        storage.selectFolder()
        // самая старая версия: копии и избранное в корне Black Desert
        val loose = (game / "FaceTexture_backup" / "2026-11-01_10-00-00").createDirectories()
        (loose / "101.bmp").writeBytes(byteArrayOf(1))
        (game / "Favorite FaceTexture" / "2026-11-02_10-00-00").createDirectories()
        // прошлая версия: BDO Tools\portraits с копиями по аккаунтам и файлами портретов
        val legacy = game / "BDO Tools" / "portraits"
        ((legacy / "FaceTexture_backup" / "222" / "2026-11-03_10-00-00").createDirectories() / "201.bmp").writeBytes(byteArrayOf(2))
        (legacy / "101.txt").writeBytes(byteArrayOf(4))
        (legacy / "201.txt").writeBytes(byteArrayOf(5))
        ((game / "BDO Tools" / "lobby_backup" / ACCOUNT).createDirectories() / "1.xml").writeBytes(byteArrayOf(6))

        storage.adoptLooseFolders(ACCOUNT, setOf("101"))

        assertFalse((game / "FaceTexture_backup").exists())
        assertFalse((game / "Favorite FaceTexture").exists())
        assertFalse((game / "BDO Tools" / "lobby_backup").exists())
        assertEquals("2026-11-01_10-00-00\t101.bmp", storage.backupListing(ACCOUNT))
        assertEquals("2026-11-02_10-00-00\t", storage.favoriteListing(ACCOUNT))
        assertEquals(listOf("2026-11-03_10-00-00"), parseBackupList(storage.backupListing("222")).map { it.name })
        assertContentEquals(byteArrayOf(4), storage.readToolFile(ACCOUNT, "101.txt"))
        assertTrue((tool / ACCOUNT / "lobby_backup" / "1.xml").exists())
        // украшение чужого персонажа ждёт открытия своего аккаунта
        assertTrue((legacy / "201.txt").exists())

        val created = storage.createBackup(ACCOUNT)
        assertTrue((tool / ACCOUNT / "FaceTexture_backup" / created).exists())
    }

    /** `Black Desert\BDO Tools\portraitConstructor` — папка тулзы, в ней папки аккаунтов. */
    private val tool get() = game / "BDO Tools" / "portraitConstructor"

    private companion object {
        const val ACCOUNT = "111"
    }
}
