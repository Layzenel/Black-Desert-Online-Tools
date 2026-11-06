package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import java.awt.Cursor

private val diagonalResize = PointerIcon(Cursor(Cursor.NE_RESIZE_CURSOR))

@Composable
actual fun Modifier.resizeCursor(dragging: Boolean): Modifier = pointerHoverIcon(diagonalResize)
