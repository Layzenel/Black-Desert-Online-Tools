package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.fps.GameFpsSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Браузер не видит кадров игры: FPS мерит только настольная версия. */
internal class BrowserGameFpsSource : GameFpsSource {
    override val available: Boolean = false
    override fun watch(): Flow<Int?> = emptyFlow()
}

internal actual fun createGameFpsSource(): GameFpsSource = BrowserGameFpsSource()
