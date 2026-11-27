package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditMovesTest {
    private val frame = Edit.Frame(ImageRect(0f, 0f, 10f, 10f), 0xFFFF0000, 2f)
    private val arrow = Edit.Arrow(ImagePoint(1f, 1f), ImagePoint(5f, 5f), 0xFFFF0000, 2f)

    @Test
    fun moveReplacesEditAndUndoBringsItBack() {
        val history = EditHistory().add(frame).add(arrow)
        val moved = history.replace(0, frame.movedBy(5f, 3f))
        assertEquals(ImageRect(5f, 3f, 15f, 13f), (moved.edits[0] as Edit.Frame).rect)
        assertEquals(history.edits, moved.undo().edits)
        assertEquals(moved.edits, moved.undo().redo().edits)
    }

    @Test
    fun removeAndClearAreUndoable() {
        val history = EditHistory().add(frame).add(arrow)
        assertEquals(listOf<Edit>(frame), history.remove(1).edits)
        val cleared = history.cleared()
        assertTrue(cleared.edits.isEmpty())
        assertEquals(history.edits, cleared.undo().edits)
    }

    @Test
    fun cropMovesOnlyInsideBounds() {
        val bounds = ImageRect(0f, 0f, 100f, 100f)
        val crop = ImageRect(10f, 10f, 30f, 30f)
        assertEquals(ImageRect(80f, 0f, 100f, 20f), crop.movedWithin(500f, -50f, bounds))
    }

    @Test
    fun newCropStaysInsidePreviousOne() {
        val previous = ImageRect(10f, 10f, 50f, 50f)
        assertEquals(ImageRect(10f, 20f, 50f, 50f), ImageRect(0f, 20f, 80f, 90f).within(previous))
    }
}
