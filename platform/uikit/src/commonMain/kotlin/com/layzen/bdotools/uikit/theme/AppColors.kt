package com.layzen.bdotools.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Палитра дизайн-системы. Значения сняты пипеткой со скриншотов окон игры
 * (меню, настройки, выбор персонажа) в `.claude/references/ui`.
 */
@Immutable
data class AppColors(
    // Фоны, от самого глубокого к самому приподнятому
    val backgroundDeep: Color,
    val backgroundSurface: Color,
    val backgroundRaised: Color,
    val backgroundItem: Color,
    val backgroundInput: Color,
    val backgroundSelected: Color,

    // Шапка окна
    val titleBar: Color,
    val titleBarDark: Color,
    val titleBarHighlight: Color,

    // Кнопки
    val button: Color,
    val buttonHover: Color,
    val buttonPressed: Color,
    val buttonBorder: Color,

    // Линии
    val border: Color,
    val borderSelected: Color,
    val divider: Color,
    val track: Color,

    // Текст
    val textPrimary: Color,
    val textBeige: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val textButton: Color,

    // Акценты
    val accentGold: Color,
    val accentGoldPale: Color,
    val danger: Color,
    val badgeNew: Color,
    val badgeHot: Color,
    val onBadge: Color,
)

val DefaultAppColors = AppColors(
    backgroundDeep = Color(0xFF18181A),
    backgroundSurface = Color(0xFF222226),
    backgroundRaised = Color(0xFF2B2B2F),
    backgroundItem = Color(0xFF34343A),
    backgroundInput = Color(0xFF1E1E21),
    backgroundSelected = Color(0xFF4F433C),

    titleBar = Color(0xFF775D3A),
    titleBarDark = Color(0xFF5E4A2E),
    titleBarHighlight = Color(0xFFC0A850),

    button = Color(0xFF44444E),
    buttonHover = Color(0xFF52525D),
    buttonPressed = Color(0xFF38383F),
    buttonBorder = Color(0xFF4E4F56),

    border = Color(0xFF39393F),
    borderSelected = Color(0xFF685048),
    divider = Color(0xFF353536),
    track = Color(0xFF101011),

    textPrimary = Color(0xFFF8F0D8),
    textBeige = Color(0xFFE0C8A0),
    textMuted = Color(0xFFA89068),
    textDisabled = Color(0xFF686868),
    textButton = Color(0xFFE8E8E8),

    accentGold = Color(0xFFF8C038),
    accentGoldPale = Color(0xFFD8B080),
    danger = Color(0xFFD86048),
    badgeNew = Color(0xFFD09820),
    badgeHot = Color(0xFFD05020),
    onBadge = Color(0xFFF8F8F8),
)
