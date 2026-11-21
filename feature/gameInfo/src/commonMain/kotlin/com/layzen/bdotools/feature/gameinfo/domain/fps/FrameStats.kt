package com.layzen.bdotools.feature.gameinfo.domain.fps

import kotlin.math.ceil

/**
 * Кадры игры за последние секунды: сколько их, насколько ровно они шли и где были рывки.
 *
 * @property fps кадров за последнюю секунду
 * @property lowFps «1% low»: FPS, если бы вся секунда шла так же медленно, как худший 1 % кадров окна, — насколько
 * плавно, а не насколько быстро
 * @property worstMs самый долгий кадр окна, мс
 * @property freezes фризы — кадры дольше [FREEZE_FACTOR] средних
 * @property frameTimesMs время каждого кадра окна по порядку, мс — для графика
 */
data class FrameStats(
    val fps: Int,
    val lowFps: Int,
    val worstMs: Double,
    val freezes: Int,
    val frameTimesMs: List<Double>,
) {
    /** Среднее время кадра окна, мс. */
    val averageMs: Double get() = frameTimesMs.average()

    companion object {
        /** Кадр дольше стольких средних — фриз. */
        const val FREEZE_FACTOR = 2.0

        private const val SECOND_MS = 1000.0
        private const val LOW_SHARE = 0.01

        /**
         * Статистика по моментам вывода кадров [framesMs] (мс, по возрастанию); `null` — меньше двух кадров, считать
         * нечего.
         */
        fun of(framesMs: List<Double>): FrameStats? {
            if (framesMs.size < 2) return null
            val times = framesMs.zipWithNext { previous, next -> next - previous }
            val last = framesMs.last()
            val average = times.average()
            val worstShare = times.sortedDescending().take(ceil(times.size * LOW_SHARE).toInt())
            return FrameStats(
                fps = framesMs.count { last - it < SECOND_MS },
                lowFps = (SECOND_MS / worstShare.average()).toInt(),
                worstMs = times.max(),
                freezes = times.count { it > average * FREEZE_FACTOR },
                frameTimesMs = times,
            )
        }
    }
}
