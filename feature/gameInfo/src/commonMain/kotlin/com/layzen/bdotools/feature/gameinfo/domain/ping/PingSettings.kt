package com.layzen.bdotools.feature.gameinfo.domain.ping

/**
 * Как показывать пинг и FPS поверх игры (одно окно).
 *
 * @property shown показывать пинг
 * @property fpsShown показывать FPS справа от пинга; действует, только пока программа запущена от администратора
 * @property scale размер в процентах
 * @property opacity непрозрачность в процентах
 */
data class PingSettings(
    val shown: Boolean = false,
    val scale: Int = 100,
    val opacity: Int = 100,
    val fpsShown: Boolean = false,
)
