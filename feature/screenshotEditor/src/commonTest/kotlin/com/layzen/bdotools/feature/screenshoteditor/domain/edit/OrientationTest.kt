package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.test.Test
import kotlin.test.assertEquals

class OrientationTest {
    private val width = 160f
    private val height = 90f
    private val point = ImagePoint(10f, 20f)

    @Test
    fun displayAndBackAreInverse() {
        for (turns in 0..3) for (mirrored in listOf(false, true)) {
            val orientation = Orientation(turns, mirrored)
            val shown = orientation.toDisplay(point, width, height)
            assertEquals(point, orientation.toImage(shown, width, height), "$orientation")
        }
    }

    @Test
    fun quarterTurnPutsTopLeftToTopRight() {
        val turned = Orientation(1)
        assertEquals(ImagePoint(height, 0f), turned.toDisplay(ImagePoint(0f, 0f), width, height))
        assertEquals(height to width, turned.size(width, height))
    }

    @Test
    fun fourTurnsAreIdentity() {
        assertEquals(Orientation(), Orientation().rotated().rotated().rotated().rotated())
    }

    @Test
    fun flipMirrorsWhatIsShown() {
        // повёрнутая набок картинка, отражённая по горизонтали экрана: левый край показа становится правым
        val turned = Orientation(1)
        val flipped = turned.flipped()
        val (shownWidth, _) = turned.size(width, height)
        val before = turned.toDisplay(point, width, height)
        val after = flipped.toDisplay(point, width, height)
        assertEquals(shownWidth - before.x, after.x, 0.001f)
        assertEquals(before.y, after.y, 0.001f)
    }
}
