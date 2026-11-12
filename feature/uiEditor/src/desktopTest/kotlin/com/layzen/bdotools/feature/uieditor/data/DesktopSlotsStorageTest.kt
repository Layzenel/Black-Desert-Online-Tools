package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsError
import com.layzen.bdotools.feature.uieditor.domain.error.SlotsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.prefs.Preferences
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.setLastModifiedTime
import kotlin.io.path.writeBytes
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/** Настольное хранилище на временной папке: те же пути, копии и ошибки, что у моста в браузере. */
class DesktopSlotsStorageTest {

    private val documents: Path = Files.createTempDirectory("bdo-slots-test")
    private val game: Path = (documents / "Black Desert").createDirectories()
    private val preferences = Preferences.userRoot().node("com/layzen/bdotools/test/slots/${System.nanoTime()}")
    private val storage = DesktopSlotsStorage(GameFolderSettings(preferences) { null }) { documents }

    private val accountFile: Path = (game / "UserCache" / "3537922").createDirectories() / "gamevariable.xml"

    /** Как у игры: метка BOM, надпись слота в Windows-1251 и подпись склада в UTF-8 в одном файле. */
    private fun gameBytes(scale: String): ByteArray =
        byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            "<UiScale Value=\"$scale\"/>\r\n<SkillCoolTimeMemo SkillNo=\"855\" Memo=\"".encodeToByteArray() +
            "Удар щитом".toByteArray(charset("windows-1251")) +
            "\"/>\r\n<Warehouse regionKey=\"32\" memo=\"Свалка\"/>\r\n".encodeToByteArray()

    init {
        accountFile.writeBytes(gameBytes("0.8"))
        val character = (game / "UserCache" / "3537922" / "11" / "4000").createDirectories() / "gameVariable.xml"
        character.writeBytes("<UIData Index=\"89\" IsShow=\"true\"/>\r\n".encodeToByteArray())
    }

    @AfterTest
    fun cleanUp() {
        preferences.removeNode()
        documents.toFile().deleteRecursively()
    }

    @Test
    fun picksBlackDesertInsideDocuments() = runBlocking {
        assertEquals(FolderAccess.NotSelected, storage.access())
        assertEquals(FolderAccess.Granted("Black Desert"), storage.selectFolder())
        assertEquals(FolderAccess.Granted("Black Desert"), storage.access())
    }

    @Test
    fun readsAndWritesTextByteForByte() = runBlocking {
        storage.selectFolder()
        assertEquals(listOf("3537922"), storage.accounts())
        val file = storage.readGameVariable("3537922")

        val backup = storage.writeGameVariable("3537922", file.text.replace("0.8", "1.0"), file.modified)

        assertContentEquals(gameBytes("1.0"), accountFile.readBytes())
        assertEquals("BDO Tools\\uiEditor\\3537922\\backups\\", backup.substringBeforeLast('\\') + "\\")
        assertEquals(1, (tool / "3537922" / "backups").listDirectoryEntries().size)
    }

    @Test
    fun refusesToWriteFileChangedByGame() = runBlocking {
        storage.selectFolder()
        val file = storage.readGameVariable("3537922")
        accountFile.setLastModifiedTime(FileTime.fromMillis(file.modified + 5_000))

        val error = assertFailsWith<SlotsException> { storage.writeGameVariable("3537922", "x", file.modified) }

        assertEquals(SlotsError.FileChanged, error.error)
    }

    @Test
    fun listsCharacterFilesWithTheirOwnNames() = runBlocking {
        storage.selectFolder()
        assertEquals(listOf("11/4000/gameVariable.xml"), storage.characterFiles("3537922"))
        storage.writeCharacterFile("3537922", "11/4000/gameVariable.xml", "<UIData Index=\"89\" IsShow=\"false\"/>\r\n")
        assertEquals("<UIData Index=\"89\" IsShow=\"false\"/>\r\n", storage.readCharacterFile("3537922", "11/4000/gameVariable.xml"))
    }

    @Test
    fun keepsPresetsInAccountFolder() = runBlocking {
        storage.selectFolder()
        storage.writePreset("3537922", "Моя.json", "{}")
        assertEquals(listOf("Моя.json"), storage.presetFiles("3537922"))
        assertEquals(emptyList(), storage.presetFiles("111"))
        assertEquals("{}", storage.readPreset("3537922", "Моя.json"))
        assertTrue((tool / "3537922" / "presets" / "Моя.json").exists())
        storage.deletePreset("3537922", "Моя.json")
        assertEquals(emptyList(), storage.presetFiles("3537922"))
    }

    @Test
    fun legacySlotsFolderMovesIntoAccountFolders() = runBlocking {
        storage.selectFolder()
        // версия до аккаунтов: пресеты прямо в presets, подложка — файл, копии с аккаунтом в имени
        val slots = (game / "BDO Tools" / "slots").createDirectories()
        ((slots / "presets").createDirectories() / "Старый.json").writeBytes("{}".encodeToByteArray())
        (slots / "background").writeBytes(byteArrayOf(7))
        ((slots / "backups").createDirectories() / "gamevariable_222_2026-11-01_10-00-00.xml").writeBytes(byteArrayOf(1))
        // прошлая версия: пресеты другого аккаунта уже в его папке
        ((slots / "presets" / "222").createDirectories() / "Чужой.json").writeBytes("{}".encodeToByteArray())

        storage.adoptLooseFiles("3537922")

        assertFalse(slots.exists())
        assertEquals(listOf("Старый.json"), storage.presetFiles("3537922"))
        assertEquals(listOf("Чужой.json"), storage.presetFiles("222"))
        assertContentEquals(byteArrayOf(7), storage.readBackground("3537922"))
        assertTrue((tool / "222" / "backups" / "gamevariable_222_2026-11-01_10-00-00.xml").exists())
        assertNull(storage.readBackground("111"))
        storage.writeBackground("111", byteArrayOf(8))
        assertContentEquals(byteArrayOf(8), storage.readBackground("111"))
    }

    /** `Black Desert\BDO Tools\uiEditor` — папка тулзы, в ней папки аккаунтов. */
    private val tool get() = game / "BDO Tools" / "uiEditor"
}
