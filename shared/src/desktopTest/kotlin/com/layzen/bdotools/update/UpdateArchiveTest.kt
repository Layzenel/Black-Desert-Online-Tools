package com.layzen.bdotools.update

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateArchiveTest {

    private val dir: Path = Files.createTempDirectory("update-archive-test")

    @Test
    fun findsAppFolderInsideArchive() {
        // так пишет Compress-Archive: папка программы в корне, обратные косые черты
        val zip = zip("BDO Tools\\BDO Tools.exe" to "exe", "BDO Tools\\app\\app.jar" to "jar")

        val app = UpdateArchive.extract(zip, dir.resolve("out"), "BDO Tools.exe")

        assertEquals(dir.resolve("out").resolve("BDO Tools").toAbsolutePath(), app)
        assertEquals("jar", Files.readString(app.resolve("app").resolve("app.jar")))
    }

    @Test
    fun rejectsEntriesOutsideTarget() {
        val zip = zip("../evil.txt" to "x")

        assertFailsWith<IOException> { UpdateArchive.extract(zip, dir.resolve("out"), "BDO Tools.exe") }
    }

    @Test
    fun failsWithoutExecutable() {
        val zip = zip("BDO Tools/app/app.jar" to "jar")

        assertFailsWith<IOException> { UpdateArchive.extract(zip, dir.resolve("out"), "BDO Tools.exe") }
    }

    private fun zip(vararg files: Pair<String, String>): Path {
        val zip = dir.resolve("update.zip")
        ZipOutputStream(Files.newOutputStream(zip)).use { output ->
            files.forEach { (name, text) ->
                output.putNextEntry(ZipEntry(name))
                output.write(text.toByteArray())
                output.closeEntry()
            }
        }
        return zip
    }
}
