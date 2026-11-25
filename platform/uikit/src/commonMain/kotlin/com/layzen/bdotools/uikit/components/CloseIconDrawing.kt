package com.layzen.bdotools.uikit.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.layzen.bdotools.uikit.theme.CloseIcon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Всё — в долях стороны значка, чтобы рисунок был одинаковым при любом размере
private const val STAR_OUTER = 0.48f
private const val STAR_INNER = 0.22f
private const val STAR_POINTS = 5
private const val OUTLINE = 0.06f
private const val FLAKE_ARM = 0.46f
private const val FLAKE_BRANCH = 0.16f
private const val FLAKE_LINE = 0.08f
private const val LEAF_RADIUS = 0.17f
private const val LEAF_OFFSET = 0.2f
private const val PETAL_LONG = 0.48f
private const val PETAL_SHORT = 0.34f
private const val PETAL_WIDTH = 0.13f
private const val CORE_RADIUS = 0.1f

/** Рисует кнопку закрытия темы [icon] на весь холст: [color] — основной цвет, [accent] — обводка или середина. */
internal fun DrawScope.drawCloseIcon(icon: CloseIcon, color: Color, accent: Color) {
    val side = min(size.width, size.height)
    when (icon) {
        CloseIcon.Cross -> Unit
        CloseIcon.Star -> drawStar(side, color, accent)
        CloseIcon.Snowflake -> drawSnowflake(side, color, accent)
        CloseIcon.Clover -> drawClover(side, color, accent)
        CloseIcon.Flower -> drawFlower(side, color, accent)
    }
}

private fun DrawScope.drawStar(side: Float, color: Color, accent: Color) {
    val path = Path()
    repeat(STAR_POINTS * 2) { index ->
        val radius = side * if (index % 2 == 0) STAR_OUTER else STAR_INNER
        val angle = -PI / 2 + PI * index / STAR_POINTS
        val point = center + Offset((radius * cos(angle)).toFloat(), (radius * sin(angle)).toFloat())
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path, color)
    drawPath(path, accent, style = Stroke(side * OUTLINE))
}

private fun DrawScope.drawSnowflake(side: Float, color: Color, accent: Color) {
    val arm = side * FLAKE_ARM
    val branch = side * FLAKE_BRANCH
    val width = side * FLAKE_LINE
    repeat(6) { index ->
        rotate(60f * index) {
            val tip = center - Offset(0f, arm)
            drawLine(color, center, tip, width, StrokeCap.Round)
            val fork = center - Offset(0f, arm * 0.6f)
            drawLine(accent, fork, fork + Offset(-branch, -branch), width, StrokeCap.Round)
            drawLine(accent, fork, fork + Offset(branch, -branch), width, StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawClover(side: Float, color: Color, accent: Color) {
    val radius = side * LEAF_RADIUS
    val offset = side * LEAF_OFFSET
    listOf(Offset(-offset, -offset), Offset(offset, -offset), Offset(-offset, offset), Offset(offset, offset)).forEach {
        drawCircle(color, radius * 1.25f, center + it)
    }
    drawLine(accent, center - Offset(offset, offset), center + Offset(offset, offset), side * OUTLINE, StrokeCap.Round)
    drawLine(accent, center + Offset(-offset, offset), center + Offset(offset, -offset), side * OUTLINE, StrokeCap.Round)
}

private fun DrawScope.drawFlower(side: Float, color: Color, accent: Color) {
    repeat(8) { index ->
        val long = index % 2 == 0
        rotate(45f * index) { drawPetal(side * if (long) PETAL_LONG else PETAL_SHORT, side * PETAL_WIDTH, if (long) color else accent) }
    }
    drawCircle(accent, side * CORE_RADIUS, center)
}

/** Лепесток-ромб от центра вверх длиной [length]. */
private fun DrawScope.drawPetal(length: Float, width: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x - width, center.y - length * 0.45f)
        lineTo(center.x, center.y - length)
        lineTo(center.x + width, center.y - length * 0.45f)
        close()
    }
    drawPath(path, color)
}
