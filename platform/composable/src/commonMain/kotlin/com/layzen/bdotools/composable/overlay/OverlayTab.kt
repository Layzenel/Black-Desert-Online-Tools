package com.layzen.bdotools.composable.overlay

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

/**
 * Вкладка меню оверлея.
 *
 * @property order место среди вкладок всех фич: меньше — левее
 * @property title подпись вкладки
 * @property content содержимое вкладки
 */
class OverlayTab(val order: Int, val title: StringResource, val content: @Composable (OverlayScope) -> Unit)
