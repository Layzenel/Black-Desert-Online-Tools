package com.layzen.bdotools.core.folder

import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.prefs.Preferences
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalPathApi::class)
class GameFolderSettingsTest {
    private val documents: Path = Files.createTempDirectory("bdo-tools-documents")
    private val preferences: Preferences = Preferences.userRoot().node("bdotools-test-${UUID.randomUUID()}")
    private val settings = GameFolderSettings(preferences) { documents }

    @AfterTest
    fun cleanUp() {
        documents.deleteRecursively()
        preferences.removeNode()
    }

    @Test
    fun findsGameFolderInDocumentsAndRemembersIt() {
        (documents / "Black Desert" / "UserCache").createDirectories()

        assertEquals(documents / "Black Desert", settings.folder)
        assertEquals(documents / "Black Desert", GameFolderSettings(preferences) { null }.folder)
    }

    @Test
    fun ignoresFolderWithoutGameData() {
        (documents / "Black Desert").createDirectories()

        assertNull(settings.folder)
    }

    @Test
    fun chosenFolderWinsOverDetected() {
        (documents / "Black Desert" / "FaceTexture").createDirectories()
        val chosen = documents / "Other"
        settings.folder = chosen

        assertEquals(chosen, settings.folder)
    }
}
