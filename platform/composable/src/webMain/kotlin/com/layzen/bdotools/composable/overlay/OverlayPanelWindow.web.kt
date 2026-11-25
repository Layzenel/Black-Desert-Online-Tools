package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** В браузере окон поверх игры нет. */
@Composable
internal actual fun OverlayPanelWindow(open: Boolean, aside: (@Composable () -> Unit)?, content: @Composable (dragModifier: Modifier) -> Unit) = Unit
