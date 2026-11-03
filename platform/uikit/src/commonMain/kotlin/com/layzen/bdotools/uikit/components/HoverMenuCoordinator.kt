package com.layzen.bdotools.uikit.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Какое меню сейчас открыто. На странице открыто не больше одного меню: наведение на другой элемент
 * сразу закрывает предыдущее, поэтому при быстром движении курсора меню не копятся.
 */
@Stable
class HoverMenuCoordinator {
    internal var owner: Any? by mutableStateOf(null)
}

/** Общий на всё приложение [HoverMenuCoordinator]: благодаря ему открыто не больше одного меню. */
val LocalHoverMenuCoordinator = staticCompositionLocalOf { HoverMenuCoordinator() }
