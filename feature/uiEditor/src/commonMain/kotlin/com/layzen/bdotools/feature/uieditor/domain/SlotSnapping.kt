package com.layzen.bdotools.feature.uieditor.domain

import kotlin.math.abs

/** Линии, к которым прилип двигаемый элемент: вертикальная по x и горизонтальная по y, в UI-px от центра экрана. */
data class SnapGuides(val x: Double? = null, val y: Double? = null) {
    companion object {
        val None = SnapGuides()
    }
}

/** На сколько UI-px сдвинуть рамку, чтобы она встала на линии, и к каким линиям она прилипла. */
data class SnapResult(val dx: Double, val dy: Double, val guides: SnapGuides)

private class Shift(val delta: Double, val line: Double)

/**
 * Прилипание рамки [moving] к линиям [xLines] и [yLines]: её край или середина, ближе всего подошедшие к линии
 * не дальше чем на [distance] UI-px, встают на эту линию. По x и по y выбор независим.
 */
fun snapToLines(moving: SlotBounds, xLines: List<Double>, yLines: List<Double>, distance: Int): SnapResult {
    val x = nearestShift(moving.xLines, xLines, distance)
    val y = nearestShift(moving.yLines, yLines, distance)
    return SnapResult(x?.delta ?: 0.0, y?.delta ?: 0.0, SnapGuides(x?.line, y?.line))
}

/** Линия из [lines] не дальше [distance] UI-px от [edge], ближайшая к нему; `null` — такой нет. Так прилипает один край. */
fun nearestLine(edge: Double, lines: List<Double>, distance: Int): Double? = nearestShift(listOf(edge), lines, distance)?.line

private fun nearestShift(edges: List<Double>, lines: List<Double>, distance: Int): Shift? =
    edges.flatMap { edge -> lines.map { Shift(it - edge, it) } }
        .filter { abs(it.delta) <= distance }
        .minByOrNull { abs(it.delta) }
