package com.layzen.bdotools.feature.screenshoteditor.domain.edit

/** Точка, сдвинутая на [dx], [dy] пикселей картинки. */
fun ImagePoint.movedBy(dx: Float, dy: Float) = ImagePoint(x + dx, y + dy)

/** Прямоугольник, сдвинутый на [dx], [dy] пикселей картинки. */
fun ImageRect.movedBy(dx: Float, dy: Float) = ImageRect(left + dx, top + dy, right + dx, bottom + dy)

/**
 * Прямоугольник, сдвинутый на [dx], [dy], но не дальше краёв [bounds]: рамку обрезки не вытащить за картинку.
 * Больше [bounds] он не станет — его размер не меняется.
 */
fun ImageRect.movedWithin(dx: Float, dy: Float, bounds: ImageRect): ImageRect = movedBy(
    dx.coerceIn(bounds.left - left, (bounds.right - right).coerceAtLeast(bounds.left - left)),
    dy.coerceIn(bounds.top - top, (bounds.bottom - bottom).coerceAtLeast(bounds.top - top)),
)

/** Часть прямоугольника внутри [bounds]: новая обрезка не выходит за прежнюю. */
fun ImageRect.within(bounds: ImageRect) = ImageRect(
    left.coerceIn(bounds.left, bounds.right),
    top.coerceIn(bounds.top, bounds.bottom),
    right.coerceIn(bounds.left, bounds.right),
    bottom.coerceIn(bounds.top, bounds.bottom),
)

/** Та же правка, сдвинутая на [dx], [dy] пикселей картинки; обрезку, ретушь и поворот не сдвигает. */
fun Edit.movedBy(dx: Float, dy: Float): Edit = when (this) {
    is Edit.Pixelate -> copy(rect = rect.movedBy(dx, dy))
    is Edit.Arrow -> copy(from = from.movedBy(dx, dy), to = to.movedBy(dx, dy))
    is Edit.Frame -> copy(rect = rect.movedBy(dx, dy))
    is Edit.Stroke -> copy(points = points.map { it.movedBy(dx, dy) })
    is Edit.Label -> copy(at = at.movedBy(dx, dy))
    is Edit.Counter -> copy(center = center.movedBy(dx, dy))
    is Edit.Crop, is Edit.Adjust, is Edit.Orient -> this
}
