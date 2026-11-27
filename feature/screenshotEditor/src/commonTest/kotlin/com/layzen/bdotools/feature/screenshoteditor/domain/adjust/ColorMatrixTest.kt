package com.layzen.bdotools.feature.screenshoteditor.domain.adjust

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorMatrixTest {
    private val gray = floatArrayOf(100f, 100f, 100f, 255f)
    private val red = floatArrayOf(200f, 50f, 50f, 255f)

    private fun assertColor(expected: FloatArray, actual: FloatArray) {
        expected.indices.forEach { assertEquals(expected[it], actual[it], 0.5f, "channel $it") }
    }

    @Test
    fun neutralChangesNothing() {
        assertColor(red, Adjustments().colorMatrix().apply(red))
        assertTrue(Adjustments().isNeutral)
    }

    @Test
    fun exposureDoublesLight() {
        assertColor(floatArrayOf(200f, 200f, 200f, 255f), Adjustments(exposure = 100).colorMatrix().apply(gray))
    }

    @Test
    fun monoRemovesColor() {
        val mono = Adjustments(saturation = -100).colorMatrix().apply(red)
        assertEquals(mono[0], mono[1], 0.01f)
        assertEquals(mono[1], mono[2], 0.01f)
    }

    @Test
    fun warmthAddsRedTakesBlue() {
        val warm = Adjustments(temperature = 100).colorMatrix().apply(gray)
        assertTrue(warm[0] > 100f && warm[2] < 100f)
    }

    @Test
    fun contrastKeepsMidGray() {
        assertColor(floatArrayOf(128f, 128f, 128f, 255f), Adjustments(contrast = 80).colorMatrix().apply(floatArrayOf(128f, 128f, 128f, 255f)))
    }

    @Test
    fun brightnessShiftsAllChannels() {
        assertColor(floatArrayOf(164f, 164f, 164f, 255f), Adjustments(brightness = 100).colorMatrix().apply(gray))
    }

    @Test
    fun vignetteAloneKeepsColors() {
        assertTrue(!Adjustments(vignette = 50).changesColor)
    }
}
