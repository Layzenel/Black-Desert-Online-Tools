package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Окно меню оверлея: двигается отдельно от кнопки, пока его не двигали — открывается под ней. Видно, пока меню
 * раскрыто ([open]) и игра на переднем плане. [content] получает `dragModifier` — повесить на шапку меню, и
 * `alignEnd` — меню в правой половине экрана: то, что вкладка показывает рядом, ставить слева от меню.
 * В браузере ничего не показывает.
 */
@Composable
internal expect fun OverlayPanelWindow(open: Boolean, content: @Composable (dragModifier: Modifier, alignEnd: Boolean) -> Unit)
