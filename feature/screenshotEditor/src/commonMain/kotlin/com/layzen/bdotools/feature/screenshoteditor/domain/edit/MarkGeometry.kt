package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Изгиб стрелки — четверть её длины вбок от середины
private const val CURVE_BEND = 0.25f
private const val CURVE_STEPS = 24
private const val ELLIPSE_STEPS = 48
private const val STAR_POINTS = 5
private const val STAR_INNER = 0.45f

/** Точка, через которую изгибается стрелка вида [ArrowStyle.Curved]; у остальных `null`. */
val Edit.Arrow.bend: ImagePoint?
    get() {
        if (style != ArrowStyle.Curved) return null
        val dx = to.x - from.x
        val dy = to.y - from.y
        return ImagePoint((from.x + to.x) / 2 - dy * CURVE_BEND, (from.y + to.y) / 2 + dx * CURVE_BEND)
    }

/** Линия стрелки точками: прямая — два конца, изогнутая — кривая по точкам. */
val Edit.Arrow.line: List<ImagePoint>
    get() {
        val control = bend ?: return listOf(from, to)
        return (0..CURVE_STEPS).map { step ->
            val t = step.toFloat() / CURVE_STEPS
            val a = (1 - t) * (1 - t)
            val b = 2 * (1 - t) * t
            val c = t * t
            ImagePoint(a * from.x + b * control.x + c * to.x, a * from.y + b * control.y + c * to.y)
        }
    }

/** Контур фигуры рамки замкнутым многоугольником, вписанным в [Edit.Frame.rect]; у круга — по точкам. */
val Edit.Frame.outline: List<ImagePoint>
    get() = with(rect) {
        when (shape) {
            FrameShape.Rectangle, FrameShape.Marker -> listOf(ImagePoint(left, top), ImagePoint(right, top), ImagePoint(right, bottom), ImagePoint(left, bottom))
            FrameShape.Ellipse -> around(ELLIPSE_STEPS) { 1f }
            FrameShape.Rhombus -> listOf(ImagePoint(center.x, top), ImagePoint(right, center.y), ImagePoint(center.x, bottom), ImagePoint(left, center.y))
            FrameShape.Triangle -> listOf(ImagePoint(center.x, top), ImagePoint(right, bottom), ImagePoint(left, bottom))
            FrameShape.Star -> around(STAR_POINTS * 2) { if (it % 2 == 0) 1f else STAR_INNER }
        }
    }

/** [count] точек по кругу, вписанному в прямоугольник, от верха по часовой; [reach] — доля радиуса для точки. */
private fun ImageRect.around(count: Int, reach: (Int) -> Float): List<ImagePoint> = (0 until count).map { i ->
    val angle = -PI / 2 + 2 * PI * i / count
    val r = reach(i)
    ImagePoint(center.x + (cos(angle) * width / 2 * r).toFloat(), center.y + (sin(angle) * height / 2 * r).toFloat())
}
