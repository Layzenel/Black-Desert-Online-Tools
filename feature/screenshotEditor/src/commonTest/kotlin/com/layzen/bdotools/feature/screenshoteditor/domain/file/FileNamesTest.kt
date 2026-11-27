package com.layzen.bdotools.feature.screenshoteditor.domain.file

import com.layzen.bdotools.feature.screenshoteditor.domain.export.ExportFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class FileNamesTest {
    @Test
    fun editedNameKeepsGameNameAndChangesExtension() {
        assertEquals("2026-11-27_230089871_edited.jpg", editedName("2026-11-27_230089871.PNG", ExportFormat.Jpeg))
        assertEquals("shot_edited.png", editedName("shot", ExportFormat.Png))
    }

    @Test
    fun freeNameAddsNumberWhenTaken() {
        val taken = setOf("a_edited.png", "a_edited_2.png")
        assertEquals("a_edited_3.png", freeName("a_edited.png", taken::contains))
        assertEquals("b.png", freeName("b.png", taken::contains))
    }
}
