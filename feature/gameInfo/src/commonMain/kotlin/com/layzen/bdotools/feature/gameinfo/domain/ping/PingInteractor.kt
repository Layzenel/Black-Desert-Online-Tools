package com.layzen.bdotools.feature.gameinfo.domain.ping

import com.layzen.bdotools.feature.gameinfo.domain.fps.FrameStats
import com.layzen.bdotools.feature.gameinfo.domain.fps.GameFpsSource
import com.layzen.bdotools.feature.gameinfo.domain.graphics.DirectXVersion
import com.layzen.bdotools.feature.gameinfo.domain.graphics.GameDirectXSource
import com.layzen.bdotools.feature.gameinfo.domain.grid.GameScreenSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Пинг, FPS, версия DirectX и размер экрана игры и как их показывать.
 *
 * @property source замер пинга
 * @property fps замер FPS
 * @property directX версия DirectX игры
 * @property screen размер экрана игры для сетки
 * @property settings сохранённые настройки показа
 */
class PingInteractor(
    private val source: GamePingSource,
    private val fps: GameFpsSource,
    private val directX: GameDirectXSource,
    private val screen: GameScreenSource,
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

    /** См. [GameDirectXSource.watch]. */
    fun watchDirectX(): Flow<DirectXVersion?> = directX.watch()

    /** См. [GameScreenSource.uiHeight]. */
    suspend fun uiHeight(): Double? = screen.uiHeight()

    /** См. [PingSettingsRepository.settings]. */
    suspend fun settings(): PingSettings = settings.settings()

    /** См. [PingSettingsRepository.save]. */
    suspend fun save(value: PingSettings) = settings.save(value)
}
