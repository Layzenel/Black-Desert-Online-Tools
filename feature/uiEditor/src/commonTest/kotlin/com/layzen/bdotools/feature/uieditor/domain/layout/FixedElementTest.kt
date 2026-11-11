package com.layzen.bdotools.feature.uieditor.domain.layout

import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenArea
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FixedElementTest {

    private val screen = ScreenSpace(2560, 1440, 0.8)

    @Test
    fun elementsSitInBottomCorners() {
        assertEquals(ScreenArea(0.0, 1770.0, 512.0, 1800.0), FixedElement.Tips.area(screen))
        assertEquals(ScreenArea(3200.0 - 672.0, 1752.0, 3200.0, 1800.0), FixedElement.MenuBar.area(screen))
    }

    @Test
    fun combinedHpSitsTopLeftOnlyWithoutSplitHp() {
        assertEquals(ScreenArea(0.0, 0.0, 480.0, 133.0), FixedElement.CombinedHp.area(screen))
        assertTrue(FixedElement.CombinedHp.isShownWith(false))
        assertFalse(FixedElement.CombinedHp.isShownWith(true))
        assertFalse(FixedElement.CombinedHp.isShownWith(null))
        assertTrue(FixedElement.Tips.isShownWith(false))
    }
}
