package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/** Справа от элемента с заходом [overlap]; если не помещается — слева. По вертикали — от верха элемента. */
internal class SideMenuPositionProvider(private val overlap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val right = anchorBounds.right - overlap
        val x = if (right + popupContentSize.width <= windowSize.width) {
            right
        } else {
            (anchorBounds.left + overlap - popupContentSize.width).coerceAtLeast(0)
        }
        val y = anchorBounds.top.coerceAtMost((windowSize.height - popupContentSize.height).coerceAtLeast(0))
        return IntOffset(x, y)
    }
}
