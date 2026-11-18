package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize

/** В браузере окон поверх игры нет. */
@Composable
actual fun GameOverlayWindow(id: String, anchor: OverlayAnchor, size: DpSize, movable: Boolean, content: @Composable () -> Unit) = Unit
