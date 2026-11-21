package com.layzen.bdotools.uikit.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppColors

/**
 * Подложка шапки, как у окон игры: бронза светлеет книзу, по краю тёмная рамка, у правого края — бледные шевроны
 * «^», вложенные друг в друга. Для [WindowTitleBar] и [TopNavigation].
 *
 * @param border толщина рамки
 */
internal fun Modifier.titleBarBackground(colors: AppColors, border: Dp): Modifier = drawBehind {
    drawRect(Brush.verticalGradient(listOf(colors.titleBar, colors.titleBarLight)))
    clipRect { drawChevrons() }
    val stroke = border.toPx()
    drawRect(colors.titleBarDark, style = Stroke(stroke * 2))
    drawLine(Color.Black, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
}

/** Шевроны у правого края: светлые и тёмные полосы через одну, вершины сверху. */
private fun DrawScope.drawChevrons() {
    val height = size.height
    val apexX = size.width - height * CHEVRON_FROM_RIGHT
    repeat(CHEVRONS) { index ->
        val apexY = height * (CHEVRON_FIRST_TOP + CHEVRON_STEP * index)
        val path = Path().apply {
            moveTo(apexX - height * CHEVRON_HALF_WIDTH, apexY + height * CHEVRON_HALF_WIDTH)
            lineTo(apexX, apexY)
            lineTo(apexX + height * CHEVRON_HALF_WIDTH, apexY + height * CHEVRON_HALF_WIDTH)
        }
        val tint = if (index % 2 == 0) Color.White.copy(alpha = LIGHT_ALPHA) else Color.Black.copy(alpha = DARK_ALPHA)
        drawPath(path, tint, style = Stroke(height * CHEVRON_STROKE, join = StrokeJoin.Miter))
    }
}

// Шевроны — в долях высоты шапки, чтобы рисунок был одинаковым у шапок любой высоты
private const val CHEVRONS = 4
private const val CHEVRON_FROM_RIGHT = 2.6f
private const val CHEVRON_FIRST_TOP = 0.05f
private const val CHEVRON_STEP = 0.32f
private const val CHEVRON_HALF_WIDTH = 1.6f
private const val CHEVRON_STROKE = 0.16f
private const val LIGHT_ALPHA = 0.07f
private const val DARK_ALPHA = 0.08f
