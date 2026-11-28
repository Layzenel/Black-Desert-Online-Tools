package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.test.Test
import kotlin.test.assertEquals

class EditResizeTest {
    private val red = 0xFFE53935

    @Test
    fun frameCornerMovesOnlyThatCorner() {
        val frame = Edit.Frame(ImageRect(10f, 10f, 50f, 40f), red, 2f)
        val resized = frame.resized(Resize(ResizeHandle.BottomRight, frame.rect), ImagePoint(80f, 90f))
        assertEquals(Edit.Frame(ImageRect(10f, 10f, 80f, 90f), red, 2f), resized)
    }

    @Test
    fun frameCornerDraggedPastOppositeFlipsRect() {
        val frame = Edit.Frame(ImageRect(10f, 10f, 50f, 40f), red, 2f)
        val resized = frame.resized(Resize(ResizeHandle.TopLeft, frame.rect), ImagePoint(70f, 60f)) as Edit.Frame
        assertEquals(ImageRect(50f, 40f, 70f, 60f), resized.rect)
    }

    @Test
    fun arrowEndMovesOnlyThatEnd() {
        val arrow = Edit.Arrow(ImagePoint(0f, 0f), ImagePoint(10f, 10f), red, 2f)
        val resized = arrow.resized(Resize(ResizeHandle.Start, ImageRect(0f, 0f, 10f, 10f)), ImagePoint(-5f, 3f))
        assertEquals(arrow.copy(from = ImagePoint(-5f, 3f)), resized)
    }

    @Test
    fun labelGrowsFromOppositeCorner() {
        val label = Edit.Label(ImagePoint(10f, 10f), "x", red, 20f)
        val bounds = ImageRect(10f, 10f, 30f, 30f)
        val resized = label.resized(Resize(ResizeHandle.BottomRight, bounds), ImagePoint(50f, 50f)) as Edit.Label
        assertEquals(ImagePoint(10f, 10f), resized.at)
        assertEquals(40f, resized.size)
    }

    @Test
    fun restyleOfSameEditIsOneUndoStep() {
        val frame = Edit.Frame(ImageRect(0f, 0f, 10f, 10f), red, 2f)
        val history = EditHistory().add(frame)
            .restyle(0, frame.copy(width = 3f))
            .restyle(0, frame.copy(width = 4f))
        assertEquals(frame.copy(width = 4f), history.edits.single())
        assertEquals(listOf(frame), history.undo().edits)
    }

    @Test
    fun restyleAfterOtherChangeIsNewStep() {
        val frame = Edit.Frame(ImageRect(0f, 0f, 10f, 10f), red, 2f)
        val history = EditHistory().add(frame)
            .restyle(0, frame.copy(width = 3f))
            .replace(0, frame.copy(width = 3f).movedBy(1f, 1f))
            .restyle(0, frame.copy(width = 5f))
        assertEquals(frame.copy(width = 3f).movedBy(1f, 1f), history.undo().edits.single())
    }
}
