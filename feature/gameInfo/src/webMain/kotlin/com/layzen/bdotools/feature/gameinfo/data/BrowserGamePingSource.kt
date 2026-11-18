package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.ping.GamePingSource
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Браузер не видит подключений игры: пинг мерит только настольная версия. */
internal class BrowserGamePingSource : GamePingSource {
    override val supported: Boolean = false
    override fun watch(): Flow<PingReading> = emptyFlow()
}

internal actual fun createGamePingSource(): GamePingSource = BrowserGamePingSource()
