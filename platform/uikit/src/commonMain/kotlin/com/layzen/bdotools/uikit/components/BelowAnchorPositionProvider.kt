package com.layzen.bdotools.uikit.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Подсказка под элементом, по центру. У края окна сдвигается ровно настолько, чтобы влезть:
 * у Material в этом случае она выравнивается по краю элемента и уезжает от него вбок.
 * Снизу не влезает — встаёт над элементом, а если не влезает и сверху (высокая карточка) — сбоку от него.
 * Подсказка никогда не накрывает сам элемент: иначе курсор оказывается над ней, элемент теряет наведение,
 * подсказка прячется, и так по кругу — она мерцает. [gap] — зазор между элементом и подсказкой в пикселях.
 */
internal class BelowAnchorPositionProvider(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val width = popupContentSize.width
        val height = popupContentSize.height
        val centered = anchorBounds.left + (anchorBounds.width - width) / 2
        val x = centered.coerceAtMost(windowSize.width - width).coerceAtLeast(0)
        val below = anchorBounds.bottom + gap
        if (below + height <= windowSize.height) return IntOffset(x, below)
        val above = anchorBounds.top - gap - height
        if (above >= 0) return IntOffset(x, above)
        val right = anchorBounds.right + gap
        val besideX = if (right + width <= windowSize.width) right else (anchorBounds.left - gap - width).coerceAtLeast(0)
        val besideY = anchorBounds.top.coerceAtMost(windowSize.height - height).coerceAtLeast(0)
        return IntOffset(besideX, besideY)
    }
}
