package com.layzen.bdotools.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Шрифты в игре плотные и почти всегда жирные. Цвет здесь не задаётся:
 * его выбирает компонент, чтобы один стиль работал и на бежевом, и на золотом тексте.
 */
@Immutable
data class AppTypography(
    val windowTitle: TextStyle,
    val sectionTitle: TextStyle,
    val menuItem: TextStyle,
    val body: TextStyle,
    val bodyBold: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
    val badge: TextStyle,
    val value: TextStyle,
)

fun appTypography(fontFamily: FontFamily = FontFamily.Default) = AppTypography(
    windowTitle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp),
    sectionTitle = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
    menuItem = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp),
    body = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 17.sp),
    bodyBold = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 17.sp),
    caption = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp),
    button = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp),
    badge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 12.sp),
    value = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp),
)
