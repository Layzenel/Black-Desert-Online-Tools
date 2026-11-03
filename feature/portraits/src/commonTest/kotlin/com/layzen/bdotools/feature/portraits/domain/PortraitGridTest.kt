package com.layzen.bdotools.feature.portraits.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PortraitGridTest {

    @Test
    fun wrapsAfterSevenColumns() {
        val grid = PortraitGrid(count = 32)

        assertEquals(7, grid.columns)
        assertEquals(5, grid.rows)
        assertEquals(CellRect(0, 0, 624, 804), grid.cell(0))
        assertEquals(CellRect(6 * (624 + 25), 0, 624, 804), grid.cell(6))
        assertEquals(CellRect(0, 804 + 25, 624, 804), grid.cell(7))
        assertEquals(CellRect(3 * (624 + 25), 4 * (804 + 25), 624, 804), grid.cell(31))
    }

    @Test
    fun sheetSizeIncludesGapsOnlyBetweenCells() {
        val grid = PortraitGrid(count = 32)

        assertEquals(7 * 624 + 6 * 25, grid.width)
        assertEquals(5 * 804 + 4 * 25, grid.height)
    }

    @Test
    fun fewPortraitsUseFewerColumns() {
        val grid = PortraitGrid(count = 3)

        assertEquals(3, grid.columns)
        assertEquals(1, grid.rows)
        assertEquals(3 * 624 + 2 * 25, grid.width)
    }

    @Test
    fun singleMaskIsOneTexture() {
        val grid = PortraitGrid.single()

        assertEquals(624, grid.width)
        assertEquals(804, grid.height)
    }

    @Test
    fun rejectsCellOutsideGrid() {
        assertFailsWith<IllegalArgumentException> { PortraitGrid(count = 3).cell(3) }
    }

    @Test
    fun coverPlacementFillsWholeGrid() {
        val grid = PortraitGrid(count = 7)
        val placement = ImagePlacement.cover(imageWidth = 1000, imageHeight = 1000, grid = grid)

        assertEquals(grid.width / 1000f, placement.scale)
        assertEquals(grid.width / 2f, placement.centerX)
        assertEquals(grid.height / 2f, placement.centerY)
    }
}
