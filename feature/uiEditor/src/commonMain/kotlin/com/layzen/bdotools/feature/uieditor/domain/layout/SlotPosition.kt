package com.layzen.bdotools.feature.uieditor.domain.layout

/**
 * Центр слота относительно центра экрана в UI-px: x растёт вправо, y — вниз.
 * От центра, а не от угла: так раскладка одинаково стоит на мониторах с разным соотношением сторон.
 */
data class SlotPosition(val x: Int, val y: Int) {
    /** Позиция, сдвинутая на [dx], [dy] UI-px. */
    fun movedBy(dx: Int, dy: Int) = SlotPosition(x + dx, y + dy)
}
