package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.fps.GameFpsSource

/** Источник FPS игры своей платформы. */
internal expect fun createGameFpsSource(): GameFpsSource
