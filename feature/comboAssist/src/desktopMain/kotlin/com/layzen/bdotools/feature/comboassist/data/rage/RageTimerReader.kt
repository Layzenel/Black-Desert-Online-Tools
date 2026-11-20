package com.layzen.bdotools.feature.comboassist.data.rage

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.roundToInt

/**
 * Читает таймер под иконкой бафа: «59 с.» ли там — так видно, что гнев только начался (он длится 60 с; «60 с.» висит
 * меньше секунды, потом секунду «59 с.»). Таймер — белый текст с тенью прямо поверх мира, поэтому сравниваются только
 * яркие пиксели: образец (`resources/rage/timer59.png`, снят под иконкой 24 px) и снимок переводятся в маску «текст /
 * не текст», расстояние — доля несовпавших пикселей текста (0 — одинаково), лучшее со сдвигом до 1 px.
 *
 * shortcut: образец снят с русского клиента («59 с.»); для другого языка клиента нужен свой образец.
 *
 * @property iconSize сторона иконки бафа на снимке в пикселях
 */
internal class RageTimerReader(private val iconSize: Int) {
    private val scale = iconSize / REFERENCE_ICON.toDouble()
    private val width = (TEMPLATE_WIDTH * scale).roundToInt()
    private val height = (TEMPLATE_HEIGHT * scale).roundToInt()
    private val offsetX = (TEMPLATE_X * scale).roundToInt()
    private val offsetY = (TEMPLATE_Y * scale).roundToInt()
    private val template: BooleanArray = mask(scaled(load()))

    /** Показывает ли таймер под иконкой [place] на снимке [frame] «59 с.». */
    fun readsStart(frame: BufferedImage, place: BuffPlace): Boolean = distance(frame, place) <= MAX_DISTANCE

    /** Расстояние таймера под иконкой [place] на снимке [frame] до «59 с.»: 0 — совпал, 1 — ничего общего. */
    fun distance(frame: BufferedImage, place: BuffPlace): Double {
        var best = 1.0
        for (dy in -SHIFT..SHIFT) for (dx in -SHIFT..SHIFT) {
            val x = place.x + offsetX + dx
            val y = place.y + offsetY + dy
            if (x < 0 || y < 0 || x + width > frame.width || y + height > frame.height) continue
            best = minOf(best, distance(mask(frame.getSubimage(x, y, width, height))))
        }
        return best
    }

    /** Доля несовпавших пикселей среди пикселей текста образца или снимка (мера Жаккара). */
    private fun distance(candidate: BooleanArray): Double {
        var union = 0
        var differ = 0
        for (i in template.indices) {
            if (template[i] || candidate[i]) union++
            if (template[i] != candidate[i]) differ++
        }
        return if (union == 0) 1.0 else differ.toDouble() / union
    }

    /** Маска текста: белый пиксель (самый тёмный канал ярче [TEXT_LEVEL]). */
    private fun mask(image: BufferedImage): BooleanArray = BooleanArray(width * height) { i ->
        val p = image.getRGB(i % width, i / width)
        minOf(p shr 16 and 0xFF, p shr 8 and 0xFF, p and 0xFF) > TEXT_LEVEL
    }

    private fun scaled(image: BufferedImage): BufferedImage {
        if (image.width == width && image.height == height) return image
        return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).also { target ->
            target.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                drawImage(image, 0, 0, width, height, null)
                dispose()
            }
        }
    }

    private companion object {
        /** Сторона иконки, под которой снят образец, px. */
        const val REFERENCE_ICON = 24

        /** Где образец относительно левого верхнего угла иконки и какого он размера, px при иконке 24 px. */
        const val TEMPLATE_X = -1
        const val TEMPLATE_Y = 24
        const val TEMPLATE_WIDTH = 26
        const val TEMPLATE_HEIGHT = 12

        const val SHIFT = 1
        const val TEXT_LEVEL = 170

        /**
         * shortcut: порог по трём таймерам «59 с.» одного снимка; уточнить по `rage.log`, если начало гнева
         * пропускается или его находят в других таймерах.
         */
        const val MAX_DISTANCE = 0.45

        fun load(): BufferedImage =
            RageTimerReader::class.java.getResourceAsStream("/rage/timer59.png")?.use(ImageIO::read)
                ?: error("Нет образца таймера гнева")
    }
}
