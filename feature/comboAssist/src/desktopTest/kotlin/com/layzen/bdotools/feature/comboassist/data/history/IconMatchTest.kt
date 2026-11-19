package com.layzen.bdotools.feature.comboassist.data.history

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IconMatchTest {

    @Test
    fun acceptsCloseMatchesWithGap() {
        assertTrue(accepts(23.0, 54.4))
        assertTrue(accepts(35.8, 56.8))
    }

    @Test
    fun acceptsDarkerHistoryIconWithWideGap() {
        // «Мощь: Инь и Ян IV»: в истории иконка темнее, но второй кандидат далеко
        assertTrue(accepts(45.7, 85.2))
    }

    @Test
    fun rejectsForeignIconsWithoutGap() {
        assertFalse(accepts(45.0, 52.0))
        assertFalse(accepts(57.0, 90.0))
        assertFalse(accepts(36.0, 40.0))
    }
}
