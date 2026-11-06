package com.layzen.bdotools.uikit.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Подсказка под элементом, по центру. У края окна сдвигается ровно настолько, чтобы влезть:
 * у Material в этом случае она выравнивается по краю элемента и уезжает от него вбок.
 * [gap] — зазор между элементом и подсказкой в пикселях.
 */
internal class BelowAnchorPositionProvider(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val centered = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val x = centered.coerceAtMost(windowSize.width - popupContentSize.width).coerceAtLeast(0)
        return IntOffset(x, anchorBounds.bottom + gap)
    }
}
