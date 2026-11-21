package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.composable.overlay.isRunningAsAdmin
import com.layzen.bdotools.feature.gameinfo.domain.fps.GameFpsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * FPS игры `BlackDesert64.exe` по событиям показа кадров ([PresentTrace]). Сессия событий живёт, пока поток собирают;
 * процесс игры ищется заново раз в секунду (игру перезапускают).
 */
internal class DesktopGameFpsSource : GameFpsSource {
    override val available: Boolean = System.getProperty("os.name").orEmpty().startsWith("Windows") && isRunningAsAdmin()

    override fun watch(): Flow<Int?> = flow {
        val trace = PresentTrace.start()
        try {
            while (true) {
                trace?.processId = runCatching { gameProcessId() }.getOrNull()
                emit(trace?.fps())
                delay(CHECK_MS)
            }
        } finally {
            trace?.stop()
        }
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val CHECK_MS = 1000L
    }
}

internal actual fun createGameFpsSource(): GameFpsSource = DesktopGameFpsSource()
