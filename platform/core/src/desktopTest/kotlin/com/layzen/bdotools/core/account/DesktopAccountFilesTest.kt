package com.layzen.bdotools.core.account

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalPathApi::class)
class DesktopAccountFilesTest {
    private val game: Path = Files.createTempDirectory("bdo-tools-accounts")
    private val files = DesktopAccountFiles { game }

    @AfterTest
    fun cleanUp() = game.deleteRecursively()

    @Test
    fun readsGameVariableOfEveryAccountFolder() = runTest {
        (game / "UserCache" / "3537922").createDirectories()
        (game / "UserCache" / "3537922" / "gameVariable.xml").writeText("order")
        (game / "UserCache" / "Layzen").createDirectories()
        (game / "UserCache" / "gamevariable.xml").writeText("root")

        val read = files.accountFiles()

        assertEquals(listOf("3537922"), read.map { it.id })
        assertEquals("order", read.single().gameVariable)
        assertTrue(read.single().modified > 0)
    }

    @Test
    fun noUserCacheGivesNoAccounts() = runTest {
        assertTrue(files.accountFiles().isEmpty())
    }

    @Test
    fun readsPortraitOrNullWhenMissing() = runTest {
        (game / "FaceTexture").createDirectories()
        (game / "FaceTexture" / "101.bmp").writeBytes(byteArrayOf(1))

        assertContentEquals(byteArrayOf(1), files.readPortrait("101"))
        assertNull(files.readPortrait("102"))
    }

    @Test
    fun readsPortraitDecorationsOfAccountOnly() = runTest {
        val current = game / "BDO Tools" / "portraitConstructor" / "3537922" / "Current portraits"
        current.createDirectories()
        (current / "101.txt").writeText("caption=Рыбак\nbadge=fishing\n")
        (current / "101.bmp").writeBytes(byteArrayOf(1))

        assertEquals(mapOf("101" to "caption=Рыбак\nbadge=fishing\n"), files.readPortraitDecorations("3537922"))
        assertTrue(files.readPortraitDecorations("777").isEmpty())
    }
}
