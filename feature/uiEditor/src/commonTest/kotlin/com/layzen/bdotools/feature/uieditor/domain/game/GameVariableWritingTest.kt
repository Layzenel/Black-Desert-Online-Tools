package com.layzen.bdotools.feature.uieditor.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals

class GameVariableWritingTest {

    @Test
    fun replacesOnlyCoordinatesOfGivenEntries() {
        val moved = UiData(100, true, "RightBottom", 1032, 9, 0.48531249, 0.8126041293)
        val written = writeUiData(GAME_VARIABLE_SAMPLE, listOf(moved))
        val expected = GAME_VARIABLE_SAMPLE.replace(
            """<UIData Index="100" IsShow="true" PendingType="RightBottom" PosX="1082" PosY="9" RelativePosX="0.4696874917" RelativePosY="0.8126041293" IsPinHold="false"/>""",
            """<UIData Index="100" IsShow="true" PendingType="RightBottom" PosX="1032" PosY="9" RelativePosX="0.4853124900" RelativePosY="0.8126041293" IsPinHold="false"/>""",
        )
        assertEquals(expected, written)
    }

    @Test
    fun indexMatchesWholeNumber() {
        // Index="13" не должен задеть Index="130"
        val moved = UiData(13, true, "RightBottom", 1, 2, 0.5, 0.5)
        val written = writeUiData(GAME_VARIABLE_SAMPLE, listOf(moved))
        assertEquals(1, written.lines().count { it.contains("""Index="130" IsShow="false" PendingType="LeftBottom" PosX="1236"""") })
    }

    @Test
    fun keepsCrlfLineEndings() {
        val written = writeUiData(GAME_VARIABLE_SAMPLE, listOf(UiData(128, true, "LeftBottom", 1261, 425, 0.401875, 0.5774569511)))
        assertEquals(GAME_VARIABLE_SAMPLE.count { it == '\r' }, written.count { it == '\r' })
        assertEquals(GAME_VARIABLE_SAMPLE.length, written.length)
    }

    @Test
    fun writesVisibility() {
        val hidden = UiData(128, false, "LeftBottom", 1061, 425, 0.3393749893, 0.5774569511)
        val written = writeUiData(GAME_VARIABLE_SAMPLE, listOf(hidden))
        assertEquals(1, written.lines().count { it.contains("""<UIData Index="128" IsShow="false"""") })
    }

    @Test
    fun characterFileGetsOnlyVisibility() {
        val text = "<UIData Index=\"89\" IsShow=\"true\"/>\r\n<UIData Index=\"128\" IsShow=\"true\"/>\r\n"
        val expected = "<UIData Index=\"89\" IsShow=\"false\"/>\r\n<UIData Index=\"128\" IsShow=\"true\"/>\r\n"
        assertEquals(expected, writeVisibility(text, mapOf(89 to false, 100 to false)))
    }

    @Test
    fun fixedFormatHasNoExponent() {
        assertEquals("0.0000001000", 1e-7.toFixed(10))
        assertEquals("-0.2500000000", (-0.25).toFixed(10))
        assertEquals("1.0000000000", 0.99999999999.toFixed(10))
    }
}
