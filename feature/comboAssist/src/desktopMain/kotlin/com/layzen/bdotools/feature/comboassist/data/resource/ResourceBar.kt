package com.layzen.bdotools.feature.comboassist.data.resource

import com.layzen.bdotools.feature.comboassist.data.history.captureScreen
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

/**
 * Полоска ресурса класса на экране: сначала ищется в области вокруг полосок HP ([locateResourceBar], пока не найдётся —
 * раз в [RETRY_MS]), потом на каждом кадре снимается только она сама и мерится длина заливки ([measureResourceBar]).
 *
 * @property place где полоски HP и ресурса (файл аккаунта)
 */
internal class ResourceBar(private val place: ResourceBarPlace) {
    private val windowsScale: Double
    private val unit: Double
    private val area: Rectangle

    /** Найденная полоска в координатах области [area] или `null`, пока не нашлась. */
    var lock: ResourceBarLock? = null
        private set

    private var triedAtMs = -RETRY_MS

    /** Снимок области поиска, где полоска не нашлась в последний раз (для журнала), или `null`. */
    var missedShot: BufferedImage? = null

    init {
        val config = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
        windowsScale = config.defaultTransform.scaleY
        val width = (config.bounds.width * windowsScale).roundToInt()
        val height = (config.bounds.height * windowsScale).roundToInt()
        unit = place.uiScale * height / place.resolutionHeight
        area = if (place.centerX != null && place.centerY != null) {
            val cx = place.centerX * width
            val cy = place.centerY * height
            rectangle(cx - SIDE_UNITS * unit, cy - UP_UNITS * unit, cx + SIDE_UNITS * unit, cy + DOWN_UNITS * unit, width, height)
        } else {
            rectangle(0.0, 0.0, PANEL_WIDTH_UNITS * unit, PANEL_HEIGHT_UNITS * unit, width, height)
        }
    }

    /** Длина заливки в момент [nowMs], px, или `null`, если полоска ещё не нашлась. */
    fun read(robot: Robot, nowMs: Long): Int? {
        val found = lock
        if (found == null) {
            if (nowMs - triedAtMs < RETRY_MS) return null
            triedAtMs = nowMs
            val shot = captureScreen(robot, logical(area), area.width, area.height)
            lock = locateResourceBar(shot, unit)
            if (lock == null) missedShot = shot
            return lock?.let { measureResourceBar(shot, it) }
        }
        val strip = Rectangle(area.x + found.left, area.y + found.rows.first, (STRIP_UNITS * unit).roundToInt(), found.rows.count())
        val shot = captureScreen(robot, logical(strip), strip.width, strip.height)
        return measureResourceBar(shot, ResourceBarLock(0 until strip.height, 0))
    }

    /** Наименьшее заметное укорочение заливки, px. */
    val minDrop: Int get() = MIN_DROP_PX

    override fun toString(): String = "area $area, lock $lock"

    private fun logical(rect: Rectangle) = Rectangle(
        (rect.x / windowsScale).toInt(), (rect.y / windowsScale).toInt(),
        (rect.width / windowsScale).toInt().coerceAtLeast(1), (rect.height / windowsScale).toInt().coerceAtLeast(1),
    )

    private fun rectangle(left: Double, top: Double, right: Double, bottom: Double, width: Int, height: Int): Rectangle {
        val x = left.roundToInt().coerceIn(0, width - 1)
        val y = top.roundToInt().coerceIn(0, height - 1)
        return Rectangle(x, y, (right.roundToInt().coerceAtMost(width) - x).coerceAtLeast(1), (bottom.roundToInt().coerceAtMost(height) - y).coerceAtLeast(1))
    }

    private companion object {
        const val RETRY_MS = 1000L

        /** Область поиска вокруг элемента здоровья, в единицах интерфейса. */
        const val SIDE_UNITS = 300.0
        const val UP_UNITS = 100.0
        const val DOWN_UNITS = 80.0

        /** Панель слева вверху без «Разделения HP» (≈480×133 единиц) с запасом. */
        const val PANEL_WIDTH_UNITS = 700.0
        const val PANEL_HEIGHT_UNITS = 220.0

        /** Длина снимаемой полоски, в единицах: длиннее самой полоски. */
        const val STRIP_UNITS = 450.0

        /**
         * Наименьшее заметное укорочение: заливка дрожит на 1–2 px и без расхода (замер 2026-11-20, Страйкер), поэтому
         * дешёвые умения ресурсом не подтверждаются ([com.layzen.bdotools.feature.comboassist.domain.keys.KeyCommand.visibleSpend]).
         */
        const val MIN_DROP_PX = 3
    }
}
