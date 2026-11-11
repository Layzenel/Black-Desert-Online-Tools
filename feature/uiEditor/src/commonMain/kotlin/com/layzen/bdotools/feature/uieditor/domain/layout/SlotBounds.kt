package com.layzen.bdotools.feature.uieditor.domain.layout

import com.layzen.bdotools.feature.uieditor.domain.screen.position

/** Рамка элемента или группы элементов в UI-px от центра экрана: x растёт вправо, y — вниз. */
data class SlotBounds(val left: Double, val top: Double, val right: Double, val bottom: Double) {

    /** Ширина рамки. */
    val width: Double get() = right - left
    /** Высота рамки. */
    val height: Double get() = bottom - top
    /** Середина рамки по горизонтали. */
    val centerX: Double get() = (left + right) / 2
    /** Середина рамки по вертикали. */
    val centerY: Double get() = (top + bottom) / 2

    /** Вертикальные линии рамки: левый край, середина, правый край. */
    val xLines: List<Double> get() = listOf(left, centerX, right)

    /** Горизонтальные линии рамки: верх, середина, низ. */
    val yLines: List<Double> get() = listOf(top, centerY, bottom)

    companion object {
        /** Рамка элемента [id] с центром в [position]. */
        fun of(id: SlotId, position: SlotPosition) = SlotBounds(
            left = position.x - id.width / 2.0,
            top = position.y - id.height / 2.0,
            right = position.x + id.width / 2.0,
            bottom = position.y + id.height / 2.0,
        )

        /** Наименьшая рамка, в которую влезают все [bounds]; `null` для пустого списка. */
        fun union(bounds: List<SlotBounds>): SlotBounds? = bounds.takeIf { it.isNotEmpty() }?.let { list ->
            SlotBounds(list.minOf { it.left }, list.minOf { it.top }, list.maxOf { it.right }, list.maxOf { it.bottom })
        }
    }
}
