package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.math.abs

/**
 * Привязка сдвигаемой пометки: на сколько её сдвинуть ещё ([dx], [dy]), чтобы край или середина совпали с краем или
 * серединой другой пометки или картинки, и где нарисовать направляющие — вертикальную в [x], горизонтальную в [y]
 * (`null` — по этой оси не привязалась).
 */
data class Snap(val dx: Float = 0f, val dy: Float = 0f, val x: Float? = null, val y: Float? = null) {
    companion object {
        /**
         * Привязка прямоугольника [moving] к краям и серединам [targets]: ближайшее совпадение не дальше [reach]
         * пикселей картинки по каждой оси.
         */
        fun of(moving: ImageRect, targets: List<ImageRect>, reach: Float): Snap {
            val (dx, x) = closest(listOf(moving.left, moving.center.x, moving.right), targets.flatMap { listOf(it.left, it.center.x, it.right) }, reach)
            val (dy, y) = closest(listOf(moving.top, moving.center.y, moving.bottom), targets.flatMap { listOf(it.top, it.center.y, it.bottom) }, reach)
            return Snap(dx, dy, x, y)
        }

        /** Самый короткий сдвиг одной из линий [own] на одну из [lines] не дальше [reach]: сдвиг и линия. */
        private fun closest(own: List<Float>, lines: List<Float>, reach: Float): Pair<Float, Float?> {
            var best: Pair<Float, Float?> = 0f to null
            var bestDistance = reach
            own.forEach { mine ->
                lines.forEach { line ->
                    val distance = abs(line - mine)
                    // при равном расстоянии — первая совпавшая: край раньше середины
                    if (distance <= reach && (best.second == null || distance < bestDistance)) {
                        bestDistance = distance
                        best = (line - mine) to line
                    }
                }
            }
            return best
        }
    }
}
