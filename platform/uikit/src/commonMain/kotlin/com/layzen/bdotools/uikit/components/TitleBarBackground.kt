package com.layzen.bdotools.uikit.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppColors

/**
 * Подложка шапки, как у окон игры: бронза светлеет книзу, по краю тёмная рамка, у правого края — крупный бледный
 * шеврон «^» с ореолом и тёмным треугольником внутри, слева от него — диагональные штрихи. Для [WindowTitleBar] и
 * [TopNavigation].
 *
 * @param border толщина рамки
 */
internal fun Modifier.titleBarBackground(colors: AppColors, border: Dp): Modifier = drawBehind {
    drawRect(Brush.verticalGradient(listOf(colors.titleBar, colors.titleBarLight)))
    clipRect { drawChevron() }
    val stroke = border.toPx()
    drawRect(colors.titleBarDark, style = Stroke(stroke * 2))
    drawLine(Color.Black, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
}

/** Шеврон с вершиной у верхнего края (стороны под 45°) и штрихи слева от него. */
private fun DrawScope.drawChevron() {
    val height = size.height
    val apexX = size.width - height * APEX_FROM_RIGHT
    repeat(STRIPES) { index ->
        val top = apexX - height * (STRIPE_FIRST + STRIPE_STEP * index)
        drawLine(Color.White.copy(alpha = STRIPE_ALPHA), Offset(top, 0f), Offset(top + height * STRIPE_SLANT, height), height * STRIPE_WIDTH)
    }
    chevronBand(apexX, height * HALO_APEX, height * BAND_APEX, Color.White.copy(alpha = HALO_ALPHA))
    chevronBand(apexX, height * BAND_APEX, height * INNER_APEX, Color.White.copy(alpha = BAND_ALPHA))
    chevronBand(apexX, height * INNER_APEX, height, Color.Black.copy(alpha = INNER_ALPHA))
}

/** Полоса между двумя вложенными «^» с вершинами на высоте [outer] и [inner]; при [inner] у низа — треугольник. */
private fun DrawScope.chevronBand(apexX: Float, outer: Float, inner: Float, color: Color) {
    val bottom = size.height
    val path = Path().apply {
        moveTo(apexX - (bottom - outer), bottom)
        lineTo(apexX, outer)
        lineTo(apexX + (bottom - outer), bottom)
        lineTo(apexX + (bottom - inner), bottom)
        lineTo(apexX, inner)
        lineTo(apexX - (bottom - inner), bottom)
        close()
    }
    drawPath(path, color)
}

// Всё — в долях высоты шапки, чтобы рисунок был одинаковым у шапок любой высоты
private const val APEX_FROM_RIGHT = 2.75f
private const val HALO_APEX = -1.2f
private const val BAND_APEX = -0.45f
private const val INNER_APEX = 0.1f
private const val STRIPES = 3
private const val STRIPE_FIRST = 2.56f
private const val STRIPE_STEP = 0.6f
private const val STRIPE_SLANT = 0.92f
private const val STRIPE_WIDTH = 0.2f
private const val HALO_ALPHA = 0.05f
private const val BAND_ALPHA = 0.14f
private const val INNER_ALPHA = 0.12f
private const val STRIPE_ALPHA = 0.06f
