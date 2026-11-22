package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** В браузере окон поверх игры нет. */
@Composable
internal actual fun OverlayMenuWindow(open: Boolean, content: @Composable (dragModifier: Modifier, gameElevated: Boolean) -> Unit) = Unit
