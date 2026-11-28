package com.layzen.bdotools.feature.screenshoteditor.domain.edit

import kotlin.math.hypot

// Пометку не сжать в точку: дальше её не взять мышью
private const val MIN_SCALE = 0.05f

/**
 * Ручки правки и где они стоят на картинке. [bounds] — прямоугольник, который правка занимает: по его углам
 * меняют размер линии кистью и надписи. У рамки и скрытой области ручки — по углам самой области, у стрелки —
 * на концах; у обрезки, ретуши и поворота ручек нет.
 */
fun Edit.handlePoints(bounds: ImageRect): Map<ResizeHandle, ImagePoint> = when (this) {
    is Edit.Arrow -> mapOf(ResizeHandle.Start to from, ResizeHandle.End to to)
    is Edit.Frame -> rect.corners()
    is Edit.Pixelate -> rect.corners()
    is Edit.Stroke, is Edit.Label, is Edit.Counter -> bounds.corners()
    is Edit.Crop, is Edit.Adjust, is Edit.Orient -> emptyMap()
}

/**
 * Правка, у которой ручку из [resize] перетащили в [to]. Рамка и скрытая область тянутся за угол, стрелка — за конец,
 * линия кистью и надпись увеличиваются целиком от противоположного угла, толщина линий не меняется.
 */
fun Edit.resized(resize: Resize, to: ImagePoint): Edit = when (this) {
    is Edit.Arrow -> if (resize.handle == ResizeHandle.End) copy(to = to) else copy(from = to)
    is Edit.Frame -> copy(rect = ImageRect.between(rect.opposite(resize.handle), to))
    is Edit.Pixelate -> copy(rect = ImageRect.between(rect.opposite(resize.handle), to))
    is Edit.Stroke, is Edit.Label, is Edit.Counter -> {
        val anchor = resize.bounds.opposite(resize.handle)
        val corner = resize.bounds.corners()[resize.handle] ?: anchor
        val was = hypot(corner.x - anchor.x, corner.y - anchor.y)
        if (was == 0f) this else scaled(hypot(to.x - anchor.x, to.y - anchor.y) / was, anchor)
    }
    is Edit.Crop, is Edit.Adjust, is Edit.Orient -> this
}

/** Правка, увеличенная в [factor] раз от точки [around]; толщина линий та же, у надписи растут буквы. */
fun Edit.scaled(factor: Float, around: ImagePoint): Edit {
    val f = factor.coerceAtLeast(MIN_SCALE)
    val point = { p: ImagePoint -> ImagePoint(around.x + (p.x - around.x) * f, around.y + (p.y - around.y) * f) }
    val area = { r: ImageRect -> ImageRect.between(point(ImagePoint(r.left, r.top)), point(ImagePoint(r.right, r.bottom))) }
    return when (this) {
        is Edit.Arrow -> copy(from = point(from), to = point(to))
        is Edit.Frame -> copy(rect = area(rect))
        is Edit.Pixelate -> copy(rect = area(rect))
        is Edit.Stroke -> copy(points = points.map(point))
        is Edit.Label -> copy(at = point(at), size = size * f)
        is Edit.Counter -> copy(center = point(center), radius = radius * f)
        is Edit.Crop, is Edit.Adjust, is Edit.Orient -> this
    }
}

private fun ImageRect.corners() = mapOf(
    ResizeHandle.TopLeft to ImagePoint(left, top),
    ResizeHandle.TopRight to ImagePoint(right, top),
    ResizeHandle.BottomLeft to ImagePoint(left, bottom),
    ResizeHandle.BottomRight to ImagePoint(right, bottom),
)

/** Угол напротив ручки [handle]: он стоит на месте, пока тянут ручку. */
private fun ImageRect.opposite(handle: ResizeHandle) = when (handle) {
    ResizeHandle.TopLeft -> ImagePoint(right, bottom)
    ResizeHandle.TopRight -> ImagePoint(left, bottom)
    ResizeHandle.BottomLeft -> ImagePoint(right, top)
    ResizeHandle.BottomRight, ResizeHandle.Start, ResizeHandle.End -> ImagePoint(left, top)
}
