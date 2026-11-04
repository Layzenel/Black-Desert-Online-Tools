package com.layzen.bdotools.feature.slots.data

import com.layzen.bdotools.core.folder.FolderAccess
import com.layzen.bdotools.core.folder.GameFolderSettings
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.prefs.Preferences
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.setLastModifiedTime
import kotlin.io.path.writeBytes
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking

/** Настольное хранилище на временной папке: те же пути, копии и ошибки, что у моста в браузере. */
class DesktopSlotsStorageTest {

    private val documents: Path = Files.createTempDirectory("bdo-slots-test")
    private val game: Path = (documents / "Black Desert").createDirectories()
    private val preferences = Preferences.userRoot().node("com/layzen/bdotools/test/slots/${System.nanoTime()}")
    private val storage = DesktopSlotsStorage(GameFolderSettings(preferences)) { documents }

    private val bom = "﻿"
    private val accountFile: Path = (game / "UserCache" / "3537922").createDirectories() / "gamevariable.xml"

    init {
        accountFile.writeBytes("$bom<UiScale Value=\"0.8\"/>\r\n".encodeToByteArray())
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
        assertEquals("$bom<UiScale Value=\"0.8\"/>\r\n", file.text)

        val backup = storage.writeGameVariable("3537922", file.text.replace("0.8", "1.0"), file.modified)

        assertEquals("$bom<UiScale Value=\"1.0\"/>\r\n", accountFile.readBytes().decodeToString())
        assertEquals("BDO Tools\\slots\\backups\\", backup.substringBeforeLast('\\') + "\\")
        assertEquals(1, (game / "BDO Tools" / "slots" / "backups").listDirectoryEntries().size)
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
    fun keepsPresetsInToolFolder() = runBlocking {
        storage.selectFolder()
        storage.writePreset("Моя.json", "{}")
        assertEquals(listOf("Моя.json"), storage.presetFiles())
        assertEquals("{}", storage.readPreset("Моя.json"))
        storage.deletePreset("Моя.json")
        assertEquals(emptyList(), storage.presetFiles())
    }
}
