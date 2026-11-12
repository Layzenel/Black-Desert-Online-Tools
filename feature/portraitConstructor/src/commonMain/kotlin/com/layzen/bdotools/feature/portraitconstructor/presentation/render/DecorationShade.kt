package com.layzen.bdotools.feature.portraitconstructor.presentation.render

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.min
import kotlin.math.pow

/** Сколько слоёв у тени: больше — мягче край, но дольше рисовать. */
private const val SHADE_STEPS = 12

/** Плотность тени в середине при силе 100 %: как у прежнего затемнения под надписью. */
private const val SHADE_DENSITY = 0.82f

/**
 * Одна мягкая тень под надписью и значком, примерно по их контуру: каждая фигура в [shapes] (рамка значка,
 * рамка строк надписи) скругляется до капсулы, фигуры сливаются в одну, и тень набирается из [SHADE_STEPS]
 * таких слившихся контуров — от широкого (на [spread] пикселей шире) к плотному. Края мягкие, а там, где
 * значок и надпись рядом, нет двойного затемнения. [strength] — 0…1, 0 — тени нет.
 */
internal fun DrawScope.drawDecorationShade(shapes: List<Rect>, color: Color, strength: Float, spread: Float) {
    if (shapes.isEmpty() || strength <= 0f) return
    // слои накладываются: альфа одного слоя такая, чтобы в середине вышла плотность SHADE_DENSITY × strength
    val layerAlpha = 1f - (1f - SHADE_DENSITY * strength).pow(1f / SHADE_STEPS)
    for (step in 0 until SHADE_STEPS) {
        val inflate = spread * (SHADE_STEPS - step) / SHADE_STEPS
        val outline = shapes.map { capsule(it.inflate(inflate)) }.reduce { merged, next ->
            Path().apply { op(merged, next, PathOperation.Union) }
        }
        drawPath(outline, color.copy(alpha = layerAlpha))
    }
}

/** Прямоугольник со скруглением в половину меньшей стороны: у квадрата — круг, у строки — капсула. */
private fun capsule(rect: Rect): Path = Path().apply {
    val radius = min(rect.width, rect.height) / 2
    addRoundRect(RoundRect(rect, CornerRadius(radius)))
}
