package com.layzen.bdotools.uikit.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Подсказка справа от элемента, вровень с его верхом; справа не влезает — слева. По вертикали сдвигается ровно
 * настолько, чтобы влезть в окно. Элемент не накрывает (иначе подсказка мерцает, см. [BelowAnchorPositionProvider]).
 * [gap] — зазор между элементом и подсказкой в пикселях.
 */
internal class EndAnchorPositionProvider(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val right = anchorBounds.right + gap
        val x = if (right + popupContentSize.width <= windowSize.width) right else (anchorBounds.left - gap - popupContentSize.width).coerceAtLeast(0)
        val y = anchorBounds.top.coerceAtMost(windowSize.height - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}
