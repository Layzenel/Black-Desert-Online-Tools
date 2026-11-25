package com.layzen.bdotools.feature.gameinfo.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameScreenFilesTest {
    @Test
    fun heightIsDividedByUiScale() {
        assertEquals(1800.0, parseUiHeight("""<Resolution Width="2560" Height="1440"/><UiScale Value="0.8"/>"""))
    }

    @Test
    fun missingScaleMeansFullSize() {
        assertEquals(1080.0, parseUiHeight("""<Resolution Width="1920" Height="1080"/>"""))
    }

    @Test
    fun missingResolutionIsUnknown() {
        assertNull(parseUiHeight("""<UiScale Value="1"/>"""))
    }
}
