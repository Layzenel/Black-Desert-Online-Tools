package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Окно кнопки меню оверлея: своё место на экране, всегда над окнами оверлея, клики ловит, но фокус у игры не
 * забирает. Видно, только пока игра на переднем плане. [open] — меню раскрыто. [content] получает
 * `dragModifier` — повесить на кнопку, чтобы окно тянулось за ней, и `alignEnd` — в правой половине экрана меню
 * раскрывается влево: кнопка справа, меню под ней по правому краю; `gameElevated` — игра запущена от имени
 * администратора. В браузере ничего не показывает.
 */
@Composable
internal expect fun OverlayMenuWindow(open: Boolean, content: @Composable (dragModifier: Modifier, alignEnd: Boolean, gameElevated: Boolean) -> Unit)
