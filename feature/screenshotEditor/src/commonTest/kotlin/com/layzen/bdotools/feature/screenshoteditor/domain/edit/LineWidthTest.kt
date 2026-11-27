package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.test.Test
import kotlin.test.assertEquals

class LineWidthTest {
    @Test
    fun widthGrowsWithImage() {
        assertEquals(4f, lineWidth(4, 800, 600))
        assertEquals(15.36f, lineWidth(4, 3840, 2160), 0.001f)
        assertEquals(24f, labelSize(4, 800, 600))
    }
}
