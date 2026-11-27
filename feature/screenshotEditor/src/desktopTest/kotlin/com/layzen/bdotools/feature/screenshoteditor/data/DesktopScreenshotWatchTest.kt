package com.layzen.bdotools.feature.screenshoteditor.data

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.StandardOpenOption.APPEND
import java.util.prefs.Preferences
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class DesktopScreenshotWatchTest {
    private val png: ByteArray = ByteArrayOutputStream().use {
        ImageIO.write(BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB), "png", it)
        it.toByteArray()
    }

    @Test
    fun emitsScreenshotOnlyWhenGameFinishedWritingIt() = runBlocking {
        val game = Files.createTempDirectory("black-desert")
        val shots = game.resolve("ScreenShot").createDirectories()
        val preferences = Preferences.userRoot().node("bdo-tools-test/screenshot-watch")
        val watch = DesktopScreenshotWatch({ game }, preferences)
        val next = async { withTimeout(15_000) { watch.newScreenshots().first() } }
        delay(500)
        shots.resolve("notes.txt").writeText("not a screenshot")
        val file = shots.resolve("2026-11-27_230089871.PNG")
        // игра пишет файл частями: половина без конца PNG не должна выдаваться
        file.writeBytes(png.copyOfRange(0, png.size / 2))
        delay(1500)
        assertTrue(next.isActive, "half-written file must not be reported")
        Files.write(file, png.copyOfRange(png.size / 2, png.size), APPEND)
        val entry = next.await()
        assertEquals("2026-11-27_230089871.PNG", entry.name)
        assertEquals(file.toString(), entry.id)
        preferences.removeNode()
    }

    @Test
    fun galleryListsAndShrinksScreenshots() = runBlocking {
        val game = Files.createTempDirectory("black-desert")
        val shot = game.resolve("ScreenShot").createDirectories().resolve("2026-11-27_1.PNG")
        shot.writeBytes(png)
        val gallery = DesktopScreenshotGallery({ game }, {})
        val entry = gallery.list().single()
        assertEquals(shot.toString(), entry.id)
        val thumbnail = ImageIO.read(gallery.thumbnail(entry).inputStream())
        assertTrue(thumbnail.width <= 360, "thumbnail width ${thumbnail.width}")
    }
}
