package com.layzen.bdotools.feature.gameinfo.presentation.state

import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings

/**
 * Что показывать поверх игры.
 *
 * @property ping пинг в миллисекундах; `null` — не узнать или пинг не показывают
 * @property settings как показывать пинг
 * @property movable пинг в режиме перемещения: обведён рамкой и тянется мышью
 */
data class GameInfoState(val ping: Int? = null, val settings: PingSettings = PingSettings(), val movable: Boolean = false)
