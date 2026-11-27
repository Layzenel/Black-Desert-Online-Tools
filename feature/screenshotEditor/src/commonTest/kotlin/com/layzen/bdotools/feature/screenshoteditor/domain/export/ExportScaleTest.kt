package com.layzen.bdotools.feature.screenshoteditor.domain.export

import kotlin.test.Test
import kotlin.test.assertEquals

class ExportScaleTest {
    @Test
    fun scalesBothSides() {
        assertEquals(PixelSize(1920, 1080), ExportScale.Half.applyTo(3840, 2160))
        assertEquals(PixelSize(3840, 2160), ExportScale.Full.applyTo(3840, 2160))
    }

    @Test
    fun neverBelowOnePixel() {
        assertEquals(PixelSize(1, 1), ExportScale.Quarter.applyTo(1, 2))
    }
}
