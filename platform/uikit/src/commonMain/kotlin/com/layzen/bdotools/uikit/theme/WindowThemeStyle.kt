package com.layzen.bdotools.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

/**
 * Как выглядит тема окон ([com.layzen.bdotools.core.theme.WindowTheme]): шапка, украшение над ней и крестик.
 *
 * @property titleBarTop цвет шапки сверху; книзу он переходит в [titleBarBottom]
 * @property titleBarDark тёмная рамка шапки
 * @property header узор у правого края шапки вместо шеврона классической темы; `null` — шеврон
 * @property decorationLeft картинка над левым краем окна; `null` — без украшения
 * @property decorationRight картинка над правым краем окна
 * @property closeIcon рисунок кнопки закрытия
 * @property closeColor его основной цвет, [closeAccent] — второй (обводка, середина)
 */
@Immutable
data class WindowThemeStyle(
    val titleBarTop: Color,
    val titleBarBottom: Color,
    val titleBarDark: Color,
    val header: DrawableResource?,
    val decorationLeft: DrawableResource?,
    val decorationRight: DrawableResource?,
    val closeIcon: CloseIcon,
    val closeColor: Color,
    val closeAccent: Color,
)
