package com.layzen.bdotools.feature.gameinfo.domain.fps

/** Как показывать FPS в информационной панели. */
enum class FpsStyle {
    /** Одной строкой: FPS и «1% low». */
    Compact,

    /** FPS, «1% low», число фризов и график времени кадра за последние секунды. */
    Detailed,
}
