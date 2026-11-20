package com.layzen.bdotools.feature.comboassist.data.resource

import com.layzen.bdotools.feature.comboassist.data.history.WatchLog
import java.awt.Robot

/**
 * Полоска ресурса для слежения за игрой: на каждом кадре мерит её ([ResourceBar]) и копит замеры ([ResourceDrops]).
 * В журнал [log] пишется, где её ищут, нашлась ли она, а первые неудачные снимки области поиска сохраняются рядом
 * с журналом: по ним видно, почему не нашлась.
 *
 * @property bar полоска ресурса или `null`, если её места не узнать (нет файла аккаунта)
 */
internal class ResourceWatch(private val bar: ResourceBar?, private val log: WatchLog) {
    private val drops = bar?.let { ResourceDrops(it.minDrop) }
    private var started = false
    private var found = false
    private var misses = 0

    /** Замер в момент [nowMs] → замеры для подтверждения нажатий или `null`, пока полоска не нашлась. */
    fun onFrame(robot: Robot, nowMs: Long): ResourceDrops? {
        if (!started) {
            started = true
            log.line(nowMs, bar?.let { "resource bar: searching, $it" } ?: "resource bar: place unknown")
        }
        val fill = bar?.read(robot, nowMs)
        if (fill == null) {
            reportMiss(nowMs)
            return null
        }
        drops?.onFill(nowMs, fill)
        if (!found) {
            found = true
            log.line(nowMs, "resource bar: found, $bar, fill $fill")
        }
        return drops
    }

    private fun reportMiss(nowMs: Long) {
        val shot = bar?.missedShot ?: return
        bar.missedShot = null
        if (misses++ >= MAX_MISSES) return
        log.line(nowMs, "resource bar: not found")
        log.miss(nowMs, shot)
    }

    companion object {
        /** Сколько неудачных снимков сохранить: дальше они одинаковые. */
        private const val MAX_MISSES = 3

        /** Полоска по тексту файла аккаунта [accountText] (`null` — без полоски: нажатия засчитываются сразу). */
        fun of(accountText: String?, log: WatchLog): ResourceWatch =
            ResourceWatch(accountText?.let(ResourceBarPlace::parse)?.let(::ResourceBar), log)
    }
}
