package com.layzen.bdotools.feature.comboassist.data.history

import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

/**
 * Где на экране строки истории умений, в пикселях экрана, и как их читать. Раскладка строк — замер 2026-11-13
 * (2560×1440, «Размер интерфейса» 80 %); снимок ставится вокруг центра элемента и растёт вместе с интерфейсом.
 *
 * @property scale пикселей экрана на пиксель замера
 * @property logicalScale во сколько раз Windows увеличивает интерфейс (125 % → 1.25)
 * @property originX левый край снимка в пикселях экрана
 * @property originY верхний край снимка в пикселях экрана
 */
internal class HistoryGeometry(
    private val scale: Double,
    private val logicalScale: Double,
    private val originX: Int,
    private val originY: Int,
) {
    private fun px(value: Int) = (value * scale).toInt()

    /** Ширина снимка в пикселях экрана. */
    val width = px(FRAME_WIDTH)

    /** Высота снимка в пикселях экрана. */
    val height = px(FRAME_HEIGHT)

    /** Строки снимка со средней строкой истории. */
    val middleBand = px(MIDDLE_TOP) until px(MIDDLE_BOTTOM)

    /** Строки снимка с нижней строкой истории. */
    val bottomBand = px(BOTTOM_TOP) until px(BOTTOM_BOTTOM)

    /** Сколько пикселей текста значит «строка есть». */
    val minTextPixels = (MIN_TEXT_PIXELS * scale * scale).toInt()

    /** Область снимка в логических координатах Java. */
    val logicalRect = Rectangle(
        (originX / logicalScale).toInt(), (originY / logicalScale).toInt(),
        (width / logicalScale).toInt().coerceAtLeast(1), (height / logicalScale).toInt().coerceAtLeast(1),
    )

    /** Иконка нижней строки из снимка [frame]. */
    fun icon(frame: BufferedImage): BufferedImage {
        val size = px(ICON_SIZE).coerceAtLeast(1)
        return frame.getSubimage(px(ICON_X), px(ICON_Y), size, size)
    }

    /** Пикселей текста (белого или красного) правее иконки в строках [band] снимка [frame]. */
    fun textPixels(frame: BufferedImage, band: IntRange): Int {
        var count = 0
        for (y in band) for (x in px(TEXT_X) until frame.width) if (isHistoryTextPixel(frame.getRGB(x, y))) count++
        return count
    }

    override fun toString(): String = "scale %.2f, windows %.2f, rect $logicalRect".format(scale, logicalScale)

    companion object {
        private const val MEASURED_HEIGHT = 1440.0
        private const val MEASURED_UI_SCALE = 0.8

        /** Центр элемента при замере (`RelativePos` 0,0319 / 0,7474) в пикселях замера. */
        private const val MEASURED_CENTER_X = 81.7
        private const val MEASURED_CENTER_Y = 1076.3
        private const val FRAME_X = 0
        private const val FRAME_Y = 1058
        private const val FRAME_WIDTH = 242
        private const val FRAME_HEIGHT = 54
        private const val ICON_X = 1
        private const val ICON_Y = 22
        private const val ICON_SIZE = 30
        private const val TEXT_X = 32
        private const val MIDDLE_TOP = 3
        private const val MIDDLE_BOTTOM = 24
        private const val BOTTOM_TOP = 26
        private const val BOTTOM_BOTTOM = 50
        private const val MIN_TEXT_PIXELS = 40

        /**
         * Геометрия на основном экране для истории на месте [place] (`null` — место замера). Пиксели экрана —
         * настоящие, с учётом масштаба Windows.
         */
        fun forScreen(place: HistoryPlace?): HistoryGeometry {
            val config = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
            val windowsScale = config.defaultTransform.scaleY
            val width = config.bounds.width * windowsScale
            val height = config.bounds.height * windowsScale
            if (place == null) {
                val scale = height / MEASURED_HEIGHT
                return HistoryGeometry(scale, windowsScale, (FRAME_X * scale).toInt(), (FRAME_Y * scale).toInt())
            }
            val scale = place.uiScale / MEASURED_UI_SCALE * height / place.resolutionHeight
            val x = place.centerX * width + (FRAME_X - MEASURED_CENTER_X) * scale
            val y = place.centerY * height + (FRAME_Y - MEASURED_CENTER_Y) * scale
            return HistoryGeometry(scale, windowsScale, x.roundToInt().coerceAtLeast(0), y.roundToInt().coerceAtLeast(0))
        }
    }
}
