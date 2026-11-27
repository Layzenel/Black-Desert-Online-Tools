package com.layzen.bdotools.feature.screenshoteditor.domain.edit

/**
 * Поворот и отражение готовой картинки: сначала отражение по горизонтали ([mirrored]), потом [quarterTurns]
 * четвертей оборота по часовой стрелке. Правки остаются в координатах исходной картинки, а поворачивается только то,
 * как её показывают и сохраняют.
 */
data class Orientation(val quarterTurns: Int = 0, val mirrored: Boolean = false) {
    /** Бок картинки лёг на низ: ширина и высота поменялись местами. */
    val sideways: Boolean get() = quarterTurns % 2 == 1

    /** Ещё четверть оборота по часовой стрелке. */
    fun rotated() = copy(quarterTurns = (quarterTurns + 1) % 4)

    /** Отражённая по горизонтали так, как её видно сейчас. */
    fun flipped(): Orientation =
        // отражение делается до поворота: у повёрнутой набок картинки «по горизонтали» на экране — это её вертикаль
        Orientation((4 - quarterTurns) % 4, !mirrored)

    /** Ширина и высота картинки [width]×[height] после поворота. */
    fun size(width: Float, height: Float): Pair<Float, Float> = if (sideways) height to width else width to height

    /** Точка картинки [width]×[height] в повёрнутой картинке. */
    fun toDisplay(point: ImagePoint, width: Float, height: Float): ImagePoint {
        val x = if (mirrored) width - point.x else point.x
        val y = point.y
        return when (quarterTurns) {
            1 -> ImagePoint(height - y, x)
            2 -> ImagePoint(width - x, height - y)
            3 -> ImagePoint(y, width - x)
            else -> ImagePoint(x, y)
        }
    }

    /** Точка повёрнутой картинки обратно в координатах исходной [width]×[height]. */
    fun toImage(point: ImagePoint, width: Float, height: Float): ImagePoint {
        val unrotated = when (quarterTurns) {
            1 -> ImagePoint(point.y, height - point.x)
            2 -> ImagePoint(width - point.x, height - point.y)
            3 -> ImagePoint(width - point.y, point.x)
            else -> point
        }
        return if (mirrored) ImagePoint(width - unrotated.x, unrotated.y) else unrotated
    }
}
