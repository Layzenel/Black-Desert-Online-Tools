package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.ping.GamePingSource

/** Источник пинга игры своей платформы. */
internal expect fun createGamePingSource(): GamePingSource
