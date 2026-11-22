package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppColors
import com.layzen.bdotools.uikit.theme.AppShapes

/** Прозрачность низа градиента обычной кнопки. */
private const val NEUTRAL_BOTTOM_ALPHA = 0.85f

/** Где бронза положительной кнопки темнее всего — доля высоты кнопки. */
private const val POSITIVE_MIDDLE = 0.65f

/** Насколько светлеет цветная кнопка (положительная, опасная) под курсором. */
private const val HOVER_ALPHA = 0.08f

/** Насколько темнеет нажатая цветная кнопка (положительная, опасная). */
private const val PRESSED_ALPHA = 0.15f

/** Подложка [ActionButtonState.Neutral] и любой недоступной кнопки: серая с лёгким градиентом, рамка [border]. */
internal fun neutralSurface(colors: AppColors, border: Dp, enabled: Boolean, hovered: Boolean, pressed: Boolean): Modifier {
    val base = when {
        !enabled || pressed -> colors.buttonPressed
        hovered -> colors.buttonHover
        else -> colors.button
    }
    return Modifier
        .background(Brush.verticalGradient(listOf(base, base.copy(alpha = NEUTRAL_BOTTOM_ALPHA))), AppShapes.Small)
        .border(border, if (hovered && enabled) colors.textMuted else colors.buttonBorder, AppShapes.Small)
}

/**
 * Подложка [ActionButtonState.Positive]: бронза темнеет к середине и снова светлеет книзу, сверху светлая кромка,
 * по краю тёмная рамка [border]. Под курсором светлее, нажатая — темнее.
 */
internal fun positiveSurface(colors: AppColors, border: Dp, hovered: Boolean, pressed: Boolean): Modifier =
    Modifier.background(
        Brush.verticalGradient(
            0f to colors.buttonPositiveTop,
            POSITIVE_MIDDLE to colors.buttonPositiveMiddle,
            1f to colors.buttonPositiveBottom,
        ),
        AppShapes.Small,
    ).drawWithContent {
        when {
            pressed -> drawRect(Color.Black.copy(alpha = PRESSED_ALPHA))
            hovered -> drawRect(Color.White.copy(alpha = HOVER_ALPHA))
        }
        // светлая кромка сразу под тёмной рамкой
        val width = border.toPx()
        val y = width * 1.5f
        drawLine(colors.buttonPositiveEdge, Offset(width, y), Offset(size.width - width, y), width)
        drawContent()
    }.border(border, colors.windowOutline, AppShapes.Small)

/**
 * Подложка [ActionButtonState.Negative]: тёмно-красная бронза темнеет книзу, по краю тёмная рамка [border].
 * Под курсором светлее, нажатая — темнее.
 */
internal fun negativeSurface(colors: AppColors, border: Dp, hovered: Boolean, pressed: Boolean): Modifier =
    Modifier.background(Brush.verticalGradient(listOf(colors.buttonNegativeTop, colors.buttonNegativeBottom)), AppShapes.Small)
        .drawWithContent {
            when {
                pressed -> drawRect(Color.Black.copy(alpha = PRESSED_ALPHA))
                hovered -> drawRect(Color.White.copy(alpha = HOVER_ALPHA))
            }
            drawContent()
        }.border(border, colors.windowOutline, AppShapes.Small)
