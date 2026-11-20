package com.layzen.bdotools.feature.comboassist.data.rage

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Ищет на снимке три бафа гнева Чёрного духа: эталоны — снимок с экрана игры (`resources/rage/buff1..3.png`, 24 px),
 * приведённые к стороне иконки [iconSize]. Счёт — средняя разница цветов середины иконки (без рамки), чем меньше,
 * тем похожее: свои иконки дают 0–3, чужие — от 42.
 *
 * Иконки 1 и 3 — общие с обычными бафами умений (замер 2026-11-20: чужие бафы дали 0,6 и 0,8), поэтому одной
 * иконки мало: гнев — это все три подряд в одном ряду, по порядку, с шагом иконки ([RageBuffSearch.triple]).
 *
 * @property iconSize сторона иконки бафа на снимке в пикселях
 */
internal class RageBuffMatcher(private val iconSize: Int) {
    private val references: List<IntArray> = REFERENCES.map { name -> pixels(scaled(load(name))) }
    private val margin = iconSize / MARGIN_DIVISOR

    /** Шаг иконок в ряду: 26 px при иконке 24 px. */
    private val step = (iconSize * STEP_RATIO).roundToInt()

    /** Где на снимке [frame] каждый эталон и есть ли тройка гнева. */
    fun search(frame: BufferedImage): RageBuffSearch {
        if (frame.width < iconSize || frame.height < iconSize) return RageBuffSearch(references.map { emptyList() }, step)
        val image = frame.getRGB(0, 0, frame.width, frame.height, null, 0, frame.width)
        return RageBuffSearch(references.map { places(image, frame.width, frame.height, it) }, step)
    }

    /** Места эталона [reference] со счётом меньше [MATCH]: у одинаковых иконок рядом — одно, лучшее. */
    private fun places(image: IntArray, width: Int, height: Int, reference: IntArray): List<BuffPlace> {
        val found = mutableListOf<BuffPlace>()
        for (y in 0..height - iconSize) for (x in 0..width - iconSize) {
            val score = score(image, width, x, y, reference)
            if (score < MATCH) found += BuffPlace(x, y, score)
        }
        val kept = mutableListOf<BuffPlace>()
        found.sortedBy { it.score }.forEach { place ->
            if (kept.none { abs(it.x - place.x) < iconSize / 2 && abs(it.y - place.y) < iconSize / 2 }) kept += place
        }
        return kept
    }

    /**
     * Средняя разница каналов в середине иконки с левым верхним углом [x], [y]. Строки идут сверху вниз; если средняя
     * по пройденным строкам уже вдвое хуже [MATCH] — место не то, дальше не считается (у своей иконки разница везде мала).
     */
    private fun score(image: IntArray, width: Int, x: Int, y: Int, reference: IntArray): Double {
        var sum = 0L
        var count = 0
        for (dy in margin until iconSize - margin) {
            for (dx in margin until iconSize - margin) {
                val a = image[(y + dy) * width + x + dx]
                val b = reference[dy * iconSize + dx]
                sum += abs((a shr 16 and 0xFF) - (b shr 16 and 0xFF)) + abs((a shr 8 and 0xFF) - (b shr 8 and 0xFF)) + abs((a and 0xFF) - (b and 0xFF))
                count += CHANNELS
            }
            if (sum > EARLY_EXIT * MATCH * count) return Double.MAX_VALUE
        }
        return sum.toDouble() / count
    }

    private fun scaled(image: BufferedImage): BufferedImage {
        if (image.width == iconSize && image.height == iconSize && image.type == BufferedImage.TYPE_INT_RGB) return image
        return BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB).also { target ->
            target.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                drawImage(image, 0, 0, iconSize, iconSize, null)
                dispose()
            }
        }
    }

    private fun pixels(image: BufferedImage): IntArray = image.getRGB(0, 0, iconSize, iconSize, null, 0, iconSize)

    private companion object {
        val REFERENCES = listOf("buff1.png", "buff2.png", "buff3.png")

        /** Рамку иконки (её доля — 1/[MARGIN_DIVISOR] стороны) не сравнивать: она у всех бафов одинаковая. */
        const val MARGIN_DIVISOR = 8
        const val CHANNELS = 3

        /**
         * shortcut: порог совпадения — по одному снимку (свои 0–3, чужие от 42); если в игре иконки окажутся темнее,
         * поднять по `rage.log`.
         */
        const val MATCH = 15.0

        /** Во сколько раз средняя по пройденным строкам может быть хуже [MATCH], прежде чем место бросят. */
        const val EARLY_EXIT = 2
        const val STEP_RATIO = 26.0 / 24.0

        fun load(name: String): BufferedImage =
            RageBuffMatcher::class.java.getResourceAsStream("/rage/$name")?.use(ImageIO::read)
                ?: error("Нет эталона бафа гнева $name")
    }
}
