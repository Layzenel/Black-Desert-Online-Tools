package com.layzen.bdotools.feature.portraits.data

import com.layzen.bdotools.feature.portraits.domain.parseBackupList
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
    fun backupCopiesPortraitsAndIsListed() = runTest {
        storage.selectFolder()
        (game / "FaceTexture" / "101.bmp").writeBytes(byteArrayOf(1))
        (game / "FaceTexture" / "102.BMP").writeBytes(byteArrayOf(2))
        (game / "FaceTexture" / "notes.txt").writeText("x")
        assertEquals(setOf("101", "102"), storage.portraitIds().toSet())

        val first = storage.backupPortraits(listOf("101"))
        val second = storage.backupPortraits(listOf("101"))
        assertTrue(first.startsWith("FaceTexture_backup\\"))
        assertNotEquals(first, second)

        val backups = parseBackupList(storage.backupListing())
        assertEquals(2, backups.size)
        assertEquals(setOf("101"), backups.first().characterIds)
        assertContentEquals(byteArrayOf(1), storage.readBackupPortrait(backups.first().name, "101"))

        storage.deleteBackup(backups.first().name)
        assertEquals(1, parseBackupList(storage.backupListing()).size)
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

        val set = storage.createFavoriteSet()
        storage.writeFavoriteFile(set, "101.bmp", byteArrayOf(3))
        storage.writeFavoriteFile(set, "name.txt", byteArrayOf(4))
        assertEquals("$set\t101.bmp", storage.favoriteListing())
        assertContentEquals(byteArrayOf(4), storage.readFavoriteFile(set, "name.txt"))
        storage.deleteFavoriteSet(set)
        assertFalse((game / "Favorite FaceTexture" / set).exists())
    }

    @Test
    fun characterOrderFromAllAccounts() = runTest {
        storage.selectFolder()
        val order = "<CharacterOrderList Version=\"1\"><Character Id=\"101\"/>"
        (game / "UserCache" / "account").createDirectories()
        (game / "UserCache" / "account" / "gamevariable.xml").writeText("<Root>$order</CharacterOrderList></Root>")
        (game / "UserCache" / "gamevariable.xml").writeText("<Root/>")
        assertEquals(listOf(order), storage.characterOrderCandidates())
    }
}
