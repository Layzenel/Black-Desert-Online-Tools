package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppColors
import com.layzen.bdotools.uikit.theme.AppShapes

/** Насколько светлеет цветная кнопка (положительная, опасная) под курсором. */
private const val HOVER_ALPHA = 0.08f

/** Насколько темнеет нажатая кнопка. */
private const val PRESSED_ALPHA = 0.15f

/**
 * Подложка [ActionButtonState.Neutral] и любой недоступной кнопки, как в настройках игры: тёмная рамка снаружи,
 * светлая кромка внутри. Обычная — серая с лёгким градиентом, под курсором — коричневая, недоступная — плоская серая.
 */
internal fun neutralSurface(colors: AppColors, border: Dp, enabled: Boolean, hovered: Boolean, pressed: Boolean): Modifier {
    val (top, bottom, edge) = when {
        !enabled -> Triple(colors.actionButtonDisabled, colors.actionButtonDisabled, colors.actionButtonDisabledEdge)
        hovered -> Triple(colors.actionButtonHoverTop, colors.actionButtonHoverBottom, colors.actionButtonHoverEdge)
        else -> Triple(colors.actionButtonTop, colors.actionButtonBottom, colors.actionButtonEdge)
    }
    return framed(colors, border, edge, Brush.verticalGradient(listOf(top, bottom)), pressed && enabled, hovered = false)
}

/** Где бронза положительной кнопки темнее всего — доля высоты кнопки. */
private const val POSITIVE_MIDDLE = 0.65f

/**
 * Подложка [ActionButtonState.Positive]: бронза темнеет к середине и снова светлеет книзу, кромка — светлая бронза
 * сверху. Под курсором светлее, нажатая — темнее.
 */
internal fun positiveSurface(colors: AppColors, border: Dp, hovered: Boolean, pressed: Boolean): Modifier =
    framed(
        colors, border, colors.buttonPositiveTop,
        Brush.verticalGradient(0f to colors.buttonPositiveTop, POSITIVE_MIDDLE to colors.buttonPositiveMiddle, 1f to colors.buttonPositiveBottom),
        pressed, hovered,
    )

/** Подложка [ActionButtonState.Negative]: тёмно-красная бронза темнеет книзу. Под курсором светлее, нажатая — темнее. */
internal fun negativeSurface(colors: AppColors, border: Dp, hovered: Boolean, pressed: Boolean): Modifier =
    framed(
        colors, border, colors.buttonNegativeTop,
        Brush.verticalGradient(listOf(colors.buttonNegativeTop, colors.buttonNegativeBottom)),
        pressed, hovered,
    )

/** Тёмная рамка [AppColors.windowOutline], внутри кромка [edge] и заливка [fill]; затемнение нажатой и подсветка наведённой. */
private fun framed(colors: AppColors, border: Dp, edge: Color, fill: Brush, pressed: Boolean, hovered: Boolean): Modifier =
    Modifier
        .border(border, colors.windowOutline, AppShapes.Control)
        .padding(border)
        .background(fill, AppShapes.ControlInner)
        .drawWithContent {
            when {
                pressed -> drawRect(Color.Black.copy(alpha = PRESSED_ALPHA))
                hovered -> drawRect(Color.White.copy(alpha = HOVER_ALPHA))
            }
            drawContent()
        }
        .border(border, edge, AppShapes.ControlInner)
