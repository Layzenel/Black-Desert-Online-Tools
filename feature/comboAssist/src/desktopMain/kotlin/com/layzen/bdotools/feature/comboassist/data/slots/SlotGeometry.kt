package com.layzen.bdotools.feature.comboassist.data.slots

import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

/**
 * Где на экране слоты перезарядок, в настоящих пикселях, и как мерить их цветность. Слот — квадрат 50 единиц
 * интерфейса (при «Размере интерфейса» 80 % и 2560×1440 — 40 пикселей); мерится его середина без рамки.
 *
 * @property layout слоты и масштаб интерфейса
 * @property screenWidth ширина экрана в настоящих пикселях
 * @property screenHeight высота экрана в настоящих пикселях
 * @property windowsScale во сколько раз Windows увеличивает интерфейс (125 % → 1.25)
 */
internal class SlotGeometry(
    private val layout: SlotLayout,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val windowsScale: Double,
) {
    private val side = SLOT_UNITS * layout.uiScale * screenHeight / layout.resolutionHeight
    private val half = (side * SAMPLE_SHARE / 2).roundToInt().coerceAtLeast(1)
    private val iconHalf = (side * ICON_SHARE / 2).roundToInt().coerceAtLeast(1)
    private val centers = layout.slots.map { (it.centerX * screenWidth).roundToInt() to (it.centerY * screenHeight).roundToInt() }

    /** Область снимка со всеми слотами в настоящих пикселях. */
    val area = Rectangle(
        centers.minOf { it.first } - iconHalf, centers.minOf { it.second } - iconHalf,
        centers.maxOf { it.first } - centers.minOf { it.first } + 2 * iconHalf,
        centers.maxOf { it.second } - centers.minOf { it.second } + 2 * iconHalf,
    )

    /** Та же область в логических координатах Java. */
    val logicalArea = Rectangle(
        (area.x / windowsScale).toInt(), (area.y / windowsScale).toInt(),
        (area.width / windowsScale).toInt().coerceAtLeast(1), (area.height / windowsScale).toInt().coerceAtLeast(1),
    )

    /** Средняя цветность (максимум минус минимум канала) середины каждого слота на снимке [frame] области [area]. */
    fun saturations(frame: BufferedImage): IntArray = IntArray(centers.size) { i ->
        val (cx, cy) = centers[i]
        var sum = 0L
        var n = 0
        for (y in cy - half until cy + half) for (x in cx - half until cx + half) {
            val p = frame.getRGB(x - area.x, y - area.y)
            val r = p shr 16 and 0xFF
            val g = p shr 8 and 0xFF
            val b = p and 0xFF
            sum += maxOf(r, g, b) - minOf(r, g, b)
            n++
        }
        (sum / n).toInt()
    }

    /** Иконка слота с центром [centerX], [centerY] (доли экрана) со снимка [frame] области [area], без рамки слота. */
    fun crop(frame: BufferedImage, centerX: Double, centerY: Double): BufferedImage {
        val x = (centerX * screenWidth).roundToInt() - iconHalf - area.x
        val y = (centerY * screenHeight).roundToInt() - iconHalf - area.y
        return frame.getSubimage(x.coerceIn(0, frame.width - 2 * iconHalf), y.coerceIn(0, frame.height - 2 * iconHalf), 2 * iconHalf, 2 * iconHalf)
    }

    override fun toString(): String = "slots ${centers.size}, area $area"

    companion object {
        private const val SLOT_UNITS = 50.0
        private const val SAMPLE_SHARE = 0.8

        /** Иконка внутри рамки слота: рамка — пара пикселей по краю. */
        private const val ICON_SHARE = 0.9

        /** Геометрия на основном экране или `null`, если слотов нет. */
        fun forScreen(layout: SlotLayout): SlotGeometry? {
            if (layout.slots.isEmpty()) return null
            val config = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
            val scale = config.defaultTransform.scaleY
            return SlotGeometry(layout, (config.bounds.width * scale).roundToInt(), (config.bounds.height * scale).roundToInt(), scale)
        }
    }
}
