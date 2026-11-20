package com.layzen.bdotools.feature.comboassist.data.rage

import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import kotlin.math.roundToInt

/**
 * Где искать бафы гнева на экране, в настоящих пикселях: область вокруг «Списка усилений» и сторона иконки.
 * Иконка — 30 единиц интерфейса (24 px при 2560×1440 и «Размере интерфейса» 80 %, снимок 2026-11-20). Первая иконка
 * ряда — в ≈200 единицах левее центра списка, ряды растут вправо, шаг рядов ≈54 единицы (скилл `game-files`);
 * на каком ряду бафы гнева, заранее не известно, поэтому область захватывает несколько рядов.
 *
 * @property area область поиска в настоящих пикселях
 * @property logicalArea та же область в логических координатах Java (для снимка)
 * @property iconSize сторона иконки бафа в настоящих пикселях
 */
internal class RageBuffGeometry(val area: Rectangle, val logicalArea: Rectangle, val iconSize: Int) {

    override fun toString(): String = "area $area, icon $iconSize"

    companion object {
        private const val ICON_UNITS = 30.0
        private const val LEFT_UNITS = 240.0
        private const val RIGHT_UNITS = 560.0
        private const val VERTICAL_UNITS = 140.0

        /** Геометрия на основном экране для списка в месте [place]. */
        fun forScreen(place: BuffListPlace): RageBuffGeometry {
            val config = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
            val windowsScale = config.defaultTransform.scaleY
            val width = (config.bounds.width * windowsScale).roundToInt()
            val height = (config.bounds.height * windowsScale).roundToInt()
            val unit = place.uiScale * height / place.resolutionHeight
            val cx = place.centerX * width
            val cy = place.centerY * height
            val left = (cx - LEFT_UNITS * unit).roundToInt().coerceAtLeast(0)
            val top = (cy - VERTICAL_UNITS * unit).roundToInt().coerceAtLeast(0)
            val right = (cx + RIGHT_UNITS * unit).roundToInt().coerceAtMost(width)
            val bottom = (cy + VERTICAL_UNITS * unit).roundToInt().coerceAtMost(height)
            val area = Rectangle(left, top, right - left, bottom - top)
            val logical = Rectangle(
                (area.x / windowsScale).toInt(), (area.y / windowsScale).toInt(),
                (area.width / windowsScale).toInt().coerceAtLeast(1), (area.height / windowsScale).toInt().coerceAtLeast(1),
            )
            return RageBuffGeometry(area, logical, (ICON_UNITS * unit).roundToInt().coerceAtLeast(MIN_ICON))
        }

        private const val MIN_ICON = 8
    }
}
