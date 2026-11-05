package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

@Composable
actual fun Modifier.panCursor(cursor: PanCursor): Modifier = when (cursor) {
    PanCursor.None -> this
    PanCursor.Open -> pointerHoverIcon(HandCursors.open)
    PanCursor.Grabbing -> pointerHoverIcon(HandCursors.grabbing)
}

/** Курсоры-руки, нарисованные при первом обращении. */
private object HandCursors {
    val open: PointerIcon by lazy { PointerIcon(handCursor("hand-open", closed = false)) }
    val grabbing: PointerIcon by lazy { PointerIcon(handCursor("hand-grabbing", closed = true)) }
}
