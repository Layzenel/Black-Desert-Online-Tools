package com.layzen.bdotools.feature.comboassist.data.history

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * Узнаёт умение по иконке нижней строки истории: средняя разница цветов в круге (в истории иконка круглая) с каждой
 * иконкой умения, уменьшенной до размера кадра. Замер 2026-11-13/14: у знакомых умений разница 9–23 при втором
 * кандидате от 27, у чужих — 36–57 без отрыва. Поэтому решение — по порогу и отрыву от второго ([accepts]).
 * Сначала ищет среди [preferred] (умения комбо): умения с одинаковой иконкой (Крадущийся волк и Охота за жертвой)
 * так не путаются с теми, что в комбо.
 *
 * @property icons байты иконок умений (webp или png) по номеру навыка
 * @property preferred умения, среди которых искать сначала
 */
internal class IconMatcher(private val icons: Map<Int, ByteArray>, private val preferred: Set<Int>) {
    private val scaled = mutableMapOf<Int, Pair<Int, BufferedImage>>()

    /** Лучшее совпадение для кадра иконки [icon]: сначала среди [preferred], потом среди всех. */
    fun match(icon: BufferedImage): IconMatch {
        val inCombo = best(icon, icons.keys.filter { it in preferred })
        return if (inCombo.accepted) inCombo else best(icon, icons.keys.toList()).takeIf { it.accepted } ?: inCombo
    }

    /** Разница кадра [frame] с иконкой умения [skillNo] (чем меньше, тем похожее) или `null`, если иконки нет. */
    fun difference(frame: BufferedImage, skillNo: Int): Double? =
        icons[skillNo]?.let { reference(skillNo, frame.width) }?.let { score(frame, it) }

    private fun best(icon: BufferedImage, candidates: List<Int>): IconMatch {
        val size = icon.width
        val scores = candidates.mapNotNull { no -> reference(no, size)?.let { no to score(icon, it) } }.sortedBy { it.second }
        val first = scores.firstOrNull() ?: return IconMatch(null, Double.MAX_VALUE, Double.MAX_VALUE)
        return IconMatch(first.first, first.second, scores.getOrNull(1)?.second ?: Double.MAX_VALUE)
    }

    private fun reference(skillNo: Int, size: Int): BufferedImage? {
        scaled[skillNo]?.takeIf { it.first == size }?.let { return it.second }
        val decoded = decode(icons.getValue(skillNo)) ?: return null
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            drawImage(decoded, 0, 0, size, size, null)
            dispose()
        }
        scaled[skillNo] = size to image
        return image
    }

    /** webp через Skia (ImageIO его не читает), дальше — обычная картинка AWT. */
    private fun decode(bytes: ByteArray): BufferedImage? = runCatching {
        val png = Image.makeFromEncoded(bytes).encodeToData(EncodedImageFormat.PNG)?.bytes ?: return null
        ImageIO.read(ByteArrayInputStream(png))
    }.getOrNull()

    private fun score(frame: BufferedImage, reference: BufferedImage): Double {
        val size = frame.width
        val center = (size - 1) / 2.0
        val radius = size * CIRCLE_SHARE
        var sum = 0L
        var count = 0
        for (y in 0 until size) for (x in 0 until size) {
            if ((x - center) * (x - center) + (y - center) * (y - center) > radius * radius) continue
            val a = frame.getRGB(x, y)
            val b = reference.getRGB(x, y)
            for (shift in CHANNEL_SHIFTS) sum += abs((a shr shift and 0xFF) - (b shr shift and 0xFF))
            count += CHANNEL_SHIFTS.size
        }
        return if (count == 0) Double.MAX_VALUE else sum.toDouble() / count
    }

    private companion object {
        const val CIRCLE_SHARE = 0.38
        val CHANNEL_SHIFTS = intArrayOf(16, 8, 0)
    }
}
