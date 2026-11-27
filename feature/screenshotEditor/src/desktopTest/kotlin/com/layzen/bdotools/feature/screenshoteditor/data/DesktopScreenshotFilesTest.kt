package com.layzen.bdotools.feature.screenshoteditor.data

import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class DesktopScreenshotFilesTest {
    @Test
    fun savesCopiesIntoToolFolderWithoutOverwriting() = runBlocking {
        val game = Files.createTempDirectory("black-desert")
        val files = DesktopScreenshotFiles { game }

        val first = files.save("shot_edited.png", byteArrayOf(1))
        val second = files.save("shot_edited.png", byteArrayOf(2))

        val folder = game.resolve("BDO Tools").resolve("screenshotEditor")
        assertEquals(folder.resolve("shot_edited.png").toString(), first)
        assertEquals(folder.resolve("shot_edited_2.png").toString(), second)
        assertContentEquals(byteArrayOf(1), Path(first).readBytes())
        assertEquals(setOf("BDO Tools"), game.listDirectoryEntries().map { it.name }.toSet())
    }
}
