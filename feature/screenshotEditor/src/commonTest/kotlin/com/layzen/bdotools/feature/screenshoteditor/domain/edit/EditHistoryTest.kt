package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditHistoryTest {
    private val frame = Edit.Frame(ImageRect(0f, 0f, 10f, 10f), 0xFFFF0000, 2f)
    private val crop = Edit.Crop(ImageRect(5f, 5f, 50f, 50f))

    @Test
    fun undoAndRedoRestoreEdits() {
        val history = EditHistory().add(frame).add(crop)
        val undone = history.undo()
        assertEquals(listOf<Edit>(frame), undone.edits)
        assertTrue(undone.canRedo)
        assertEquals(history.edits, undone.redo().edits)
    }

    @Test
    fun newEditClearsRedo() {
        val history = EditHistory().add(frame).undo().add(crop)
        assertFalse(history.canRedo)
    }

    @Test
    fun lastCropWinsAndIsNotDrawn() {
        val later = Edit.Crop(ImageRect(0f, 0f, 20f, 20f))
        val history = EditHistory().add(crop).add(frame).add(later)
        assertEquals(later.rect, history.crop)
        assertEquals(listOf<Edit>(frame), history.drawn)
        assertNull(EditHistory().add(frame).crop)
    }

    @Test
    fun undoOnEmptyHistoryDoesNothing() {
        assertEquals(EditHistory(), EditHistory().undo().redo())
    }
}
