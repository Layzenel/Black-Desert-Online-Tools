package com.layzen.bdotools.feature.comboassist.data.history

import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.image.BufferedImage

/**
 * Где на экране строки истории умений, в пикселях экрана, и как их читать.
 *
 * @property scale пикселей экрана на пиксель замера (2560×1440)
 * @property logicalScale во сколько раз Windows увеличивает интерфейс (125 % → 1.25)
 */
internal class HistoryGeometry(private val scale: Double, private val logicalScale: Double) {
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
        (px(FRAME_X) / logicalScale).toInt(), (px(FRAME_Y) / logicalScale).toInt(),
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

        /** Геометрия основного экрана: его высота в настоящих пикселях и масштаб Windows. */
        fun forScreen(): HistoryGeometry {
            val config = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
            val windowsScale = config.defaultTransform.scaleY
            val physicalHeight = config.bounds.height * windowsScale
            return HistoryGeometry(physicalHeight / MEASURED_HEIGHT, windowsScale)
        }
    }
}
