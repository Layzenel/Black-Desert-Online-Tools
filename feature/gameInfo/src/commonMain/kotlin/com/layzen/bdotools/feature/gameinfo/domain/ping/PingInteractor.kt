package com.layzen.bdotools.feature.gameinfo.domain.ping

import com.layzen.bdotools.feature.gameinfo.domain.fps.FrameStats
import com.layzen.bdotools.feature.gameinfo.domain.fps.GameFpsSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Пинг и FPS игры и как их показывать.
 *
 * @property source замер пинга
 * @property fps замер FPS
 * @property settings сохранённые настройки показа
 */
class PingInteractor(
    private val source: GamePingSource,
    private val fps: GameFpsSource,
    private val settings: PingSettingsRepository,
) {
    /** Умеет ли платформа мерить пинг. */
    val supported: Boolean get() = source.supported

    /** См. [GamePingSource.watch]. */
    fun watch(): Flow<PingReading> = source.watch()

    /** См. [GameFpsSource.available]. */
    val fpsAvailable: Boolean get() = fps.available

    /** Кадры игры раз в секунду ([GameFpsSource.watch]); `null` — игра не рисует кадры. */
    fun watchFps(): Flow<FrameStats?> = fps.watch().map { frames -> frames?.let(FrameStats::of) }

    /** См. [PingSettingsRepository.settings]. */
    suspend fun settings(): PingSettings = settings.settings()

    /** См. [PingSettingsRepository.save]. */
    suspend fun save(value: PingSettings) = settings.save(value)
}
