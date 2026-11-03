package com.layzen.bdotools.feature.portraits.domain

/**
 * Сетка портретов как в окне выбора персонажа: [count] ячеек, не больше [maxColumns] в ряду.
 * Координаты — в пикселях текстуры, начиная с левого верхнего угла первой ячейки.
 */
data class PortraitGrid(
    val count: Int,
    val maxColumns: Int = PortraitTexture.MAX_COLUMNS,
    val cellWidth: Int = PortraitTexture.WIDTH,
    val cellHeight: Int = PortraitTexture.HEIGHT,
    val gap: Int = PortraitTexture.GAP,
) {
    init {
        require(count > 0) { "Сетка должна содержать хотя бы одну ячейку" }
        require(maxColumns > 0) { "Нужна хотя бы одна колонка" }
    }

    val columns: Int = minOf(count, maxColumns)
    val rows: Int = (count + columns - 1) / columns

    val width: Int = columns * cellWidth + (columns - 1) * gap
    val height: Int = rows * cellHeight + (rows - 1) * gap

    fun cell(index: Int): CellRect {
        require(index in 0 until count) { "Ячейки $index нет в сетке из $count" }
        val column = index % columns
        val row = index / columns
        return CellRect(
            x = column * (cellWidth + gap),
            y = row * (cellHeight + gap),
            width = cellWidth,
            height = cellHeight,
        )
    }

    companion object {
        /** Маска для замены одного портрета. */
        fun single() = PortraitGrid(count = 1)
    }
}
