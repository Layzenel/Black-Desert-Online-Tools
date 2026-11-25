package com.layzen.bdotools.uikit.theme

import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.core.theme.WindowTheme
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.theme_morning_header
import com.layzen.bdotools.uikit.resources.theme_morning_left
import com.layzen.bdotools.uikit.resources.theme_morning_right
import com.layzen.bdotools.uikit.resources.theme_spring_header
import com.layzen.bdotools.uikit.resources.theme_spring_left
import com.layzen.bdotools.uikit.resources.theme_spring_right
import com.layzen.bdotools.uikit.resources.theme_summer_header
import com.layzen.bdotools.uikit.resources.theme_summer_left
import com.layzen.bdotools.uikit.resources.theme_summer_right
import com.layzen.bdotools.uikit.resources.theme_winter_header
import com.layzen.bdotools.uikit.resources.theme_winter_left
import com.layzen.bdotools.uikit.resources.theme_winter_right

// Цвета шапок сняты со скриншотов окна настроек игры в каждой теме; картинки вырезаны из них же
private val Classic = WindowThemeStyle(
    titleBarTop = DefaultAppColors.titleBar,
    titleBarBottom = DefaultAppColors.titleBarLight,
    titleBarDark = DefaultAppColors.titleBarDark,
    header = null,
    decorationLeft = null,
    decorationRight = null,
    closeIcon = CloseIcon.Cross,
    closeColor = DefaultAppColors.textPrimary,
    closeAccent = DefaultAppColors.accentGold,
)

private val Summer = WindowThemeStyle(
    titleBarTop = Color(0xFF227A9B),
    titleBarBottom = Color(0xFF248B9C),
    titleBarDark = Color(0xFF14566E),
    header = Res.drawable.theme_summer_header,
    decorationLeft = Res.drawable.theme_summer_left,
    decorationRight = Res.drawable.theme_summer_right,
    closeIcon = CloseIcon.Star,
    closeColor = Color(0xFFE8452C),
    closeAccent = Color(0xFFFFB060),
)

private val Winter = WindowThemeStyle(
    titleBarTop = Color(0xFF43526E),
    titleBarBottom = Color(0xFF404E6A),
    titleBarDark = Color(0xFF2C374B),
    header = Res.drawable.theme_winter_header,
    decorationLeft = Res.drawable.theme_winter_left,
    decorationRight = Res.drawable.theme_winter_right,
    closeIcon = CloseIcon.Snowflake,
    closeColor = Color(0xFFF2F6FF),
    closeAccent = Color(0xFFA8C0E8),
)

private val Spring = WindowThemeStyle(
    titleBarTop = Color(0xFF496051),
    titleBarBottom = Color(0xFF4B6553),
    titleBarDark = Color(0xFF33443A),
    header = Res.drawable.theme_spring_header,
    decorationLeft = Res.drawable.theme_spring_left,
    decorationRight = Res.drawable.theme_spring_right,
    closeIcon = CloseIcon.Clover,
    closeColor = Color(0xFF9CD45C),
    closeAccent = Color(0xFFE4F5C8),
)

private val MorningLand = WindowThemeStyle(
    titleBarTop = Color(0xFF724539),
    titleBarBottom = Color(0xFF6D4034),
    titleBarDark = Color(0xFF4E2E25),
    header = Res.drawable.theme_morning_header,
    decorationLeft = Res.drawable.theme_morning_left,
    decorationRight = Res.drawable.theme_morning_right,
    closeIcon = CloseIcon.Flower,
    closeColor = Color(0xFFF0A050),
    closeAccent = Color(0xFFFFF4E4),
)

/** Как выглядит тема окон. */
val WindowTheme.style: WindowThemeStyle
    get() = when (this) {
        WindowTheme.Classic -> Classic
        WindowTheme.Summer -> Summer
        WindowTheme.Winter -> Winter
        WindowTheme.Spring -> Spring
        WindowTheme.MorningLand -> MorningLand
    }
