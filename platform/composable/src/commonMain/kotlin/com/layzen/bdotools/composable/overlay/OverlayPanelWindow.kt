package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Окно меню оверлея: двигается отдельно от кнопки, пока его не двигали — открывается под ней. Видно, пока меню
 * раскрыто ([open]) и игра на переднем плане. [content] получает `dragModifier` — повесить на шапку меню.
 * [aside] — то, что вкладка показывает рядом с меню (схема комбо): своё окно со стороны центра экрана, сквозь которое
 * проходят клики; окно меню из-за него не меняется. В браузере ничего не показывает.
 */
@Composable
internal expect fun OverlayPanelWindow(open: Boolean, aside: (@Composable () -> Unit)?, content: @Composable (dragModifier: Modifier) -> Unit)
