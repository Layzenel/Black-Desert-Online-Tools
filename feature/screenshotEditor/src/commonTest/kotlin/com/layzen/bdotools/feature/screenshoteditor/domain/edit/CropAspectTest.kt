package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.test.Test
import kotlin.test.assertEquals

class CropAspectTest {
    @Test
    fun freeCropFollowsMouseInAnyDirection() {
        assertEquals(ImageRect(10f, 20f, 100f, 80f), CropAspect.Free.rect(ImagePoint(100f, 80f), ImagePoint(10f, 20f)))
    }

    @Test
    fun wideCropKeepsSixteenByNine() {
        val rect = CropAspect.Wide.rect(ImagePoint(0f, 0f), ImagePoint(160f, 10f))
        assertEquals(ImageRect(0f, 0f, 160f, 90f), rect)
    }

    @Test
    fun ratioCropGrowsTowardsDragDirection() {
        val rect = CropAspect.Square.rect(ImagePoint(100f, 100f), ImagePoint(40f, 70f))
        assertEquals(ImageRect(40f, 40f, 100f, 100f), rect)
    }

    @Test
    fun tallDragUsesHeight() {
        val rect = CropAspect.Tall.rect(ImagePoint(0f, 0f), ImagePoint(9f, 160f))
        assertEquals(ImageRect(0f, 0f, 90f, 160f), rect)
    }

    @Test
    fun rectSnapsInsideImage() {
        assertEquals(ImageRect(0f, 3f, 50f, 40f), ImageRect(-5f, 2.6f, 70f, 40.2f).snappedInto(50, 60))
    }
}
