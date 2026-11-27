package com.layzen.bdotools.feature.screenshoteditor.domain.watch

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenshotWatchTest {
    private val pngEnd = byteArrayOf(0x49, 0x45, 0x4E, 0x44, 0xAE.toByte(), 0x42, 0x60, 0x82.toByte())

    @Test
    fun onlyGameNamesAreScreenshots() {
        assertTrue(isGameScreenshotName("2026-11-27_230089871.PNG"))
        assertTrue(isGameScreenshotName("2025-07-18_95222428.jpg"))
        assertFalse(isGameScreenshotName("2026-11-27_230089871_edited.png"))
        assertFalse(isGameScreenshotName("desktop.ini"))
        assertFalse(isGameScreenshotName("2026-11-27_230089871.PNG.tmp"))
    }

    @Test
    fun pngIsCompleteOnlyWithIendChunk() {
        assertTrue(looksComplete("a.PNG", byteArrayOf(1, 2) + pngEnd))
        assertFalse(looksComplete("a.png", byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)))
        assertTrue(looksComplete("a.jpg", byteArrayOf(1, 0xFF.toByte(), 0xD9.toByte())))
        assertTrue(looksComplete("a.bmp", ByteArray(0)))
    }

    @Test
    fun fileIsReadyWhenSizeStopsGrowingAndEndIsThere() {
        val pending = PendingScreenshots()
        val name = "2026-11-27_1.PNG"
        pending.seen(name, now = 0)
        assertFalse(pending.check(name, 100, complete = false, now = 0))
        assertFalse(pending.check(name, 200, complete = true, now = 500))
        assertTrue(pending.check(name, 200, complete = true, now = 1000))
        // игра трогает файл ещё раз — второй раз он не выдаётся
        pending.seen(name, now = 1500)
        assertFalse(pending.check(name, 200, complete = true, now = 2000))
        assertTrue(pending.waiting.isEmpty())
    }

    @Test
    fun foreignFilesAndStuckFilesAreDropped() {
        val pending = PendingScreenshots()
        pending.seen("notes.txt", now = 0)
        assertTrue(pending.waiting.isEmpty())
        pending.seen("2026-11-27_2.PNG", now = 0)
        assertFalse(pending.check("2026-11-27_2.PNG", 10, complete = false, now = 31_000))
        assertTrue(pending.waiting.isEmpty())
    }
}
