package com.layzen.bdotools.feature.gameinfo.domain.ping

/**
 * Как показывать пинг поверх игры.
 *
 * @property shown показывать пинг
 * @property scale размер в процентах
 * @property opacity непрозрачность в процентах
 */
data class PingSettings(val shown: Boolean = false, val scale: Int = 100, val opacity: Int = 100)
