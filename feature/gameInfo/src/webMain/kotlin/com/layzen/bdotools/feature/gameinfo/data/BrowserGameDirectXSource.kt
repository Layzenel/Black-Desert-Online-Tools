package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.graphics.DirectXVersion
import com.layzen.bdotools.feature.gameinfo.domain.graphics.GameDirectXSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Браузер не видит процесс игры: версию DirectX узнаёт только настольная версия. */
internal class BrowserGameDirectXSource : GameDirectXSource {
    override fun watch(): Flow<DirectXVersion?> = emptyFlow()
}

internal actual fun createGameDirectXSource(): GameDirectXSource = BrowserGameDirectXSource()
