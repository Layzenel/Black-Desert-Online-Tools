package com.layzen.bdotools.uikit.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily

/** Палитра текущей темы. Читать через [AppTheme.colors]. */
val LocalAppColors = staticCompositionLocalOf { DefaultAppColors }
/** Текстовые стили текущей темы. Читать через [AppTheme.typography]. */
val LocalAppTypography = staticCompositionLocalOf { appTypography() }
/** Отступы и размеры текущей темы. Читать через [AppTheme.dimens]. */
val LocalAppDimens = staticCompositionLocalOf { AppDimens() }

/**
 * Корневая тема. Оборачивает контент и в Material 3, чтобы стандартные
 * компоненты (DropdownMenu, Text без стиля) тоже получили тёмную палитру.
 */
@Composable
fun AppTheme(
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit,
) {
    val colors = DefaultAppColors
    val typography = appTypography(fontFamily)

    MaterialTheme(colorScheme = colors.toMaterialScheme()) {
        CompositionLocalProvider(
            LocalAppColors provides colors,
            LocalAppTypography provides typography,
            LocalAppDimens provides AppDimens(),
            LocalContentColor provides colors.textPrimary,
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = colors.accentGold,
                backgroundColor = colors.accentGold.copy(alpha = 0.3f),
            ),
            content = content,
        )
    }
}

/** Доступ к токенам темы из composable-функций: `AppTheme.colors`, `AppTheme.typography`, `AppTheme.dimens`. */
object AppTheme {
    /** Палитра текущей темы. */
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
    /** Текстовые стили текущей темы; цвет в них не заложен. */
    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current
    /** Общие отступы и размеры компонентов. */
    val dimens: AppDimens
        @Composable @ReadOnlyComposable get() = LocalAppDimens.current
}

private fun AppColors.toMaterialScheme() = darkColorScheme(
    primary = accentGold,
    onPrimary = backgroundDeep,
    secondary = titleBar,
    onSecondary = textPrimary,
    background = backgroundDeep,
    onBackground = textPrimary,
    surface = backgroundRaised,
    onSurface = textPrimary,
    surfaceVariant = backgroundItem,
    onSurfaceVariant = textBeige,
    surfaceContainer = backgroundRaised,
    outline = border,
    outlineVariant = divider,
    error = danger,
)
