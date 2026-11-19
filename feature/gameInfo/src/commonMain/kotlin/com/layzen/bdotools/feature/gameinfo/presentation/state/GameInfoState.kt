package com.layzen.bdotools.feature.gameinfo.presentation.state

import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings

/**
 * Что показывать поверх игры.
 *
 * @property ping пинг в миллисекундах; `null` — не узнать или пинг не показывают
 * @property needsAdmin пинг не собрать без прав администратора — подсказка во вкладке
 * @property settings как показывать пинг
 */
data class GameInfoState(
    val ping: Int? = null,
    val needsAdmin: Boolean = false,
    val settings: PingSettings = PingSettings(),
)
