package com.layzen.bdotools.feature.gameinfo.domain.ping

import kotlinx.coroutines.flow.Flow

/**
 * Пинг игры и как его показывать.
 *
 * @property source замер пинга
 * @property settings сохранённые настройки показа
 */
class PingInteractor(private val source: GamePingSource, private val settings: PingSettingsRepository) {
    /** Умеет ли платформа мерить пинг. */
    val supported: Boolean get() = source.supported

    /** См. [GamePingSource.watch]. */
    fun watch(): Flow<PingReading> = source.watch()

    /** См. [PingSettingsRepository.settings]. */
    suspend fun settings(): PingSettings = settings.settings()

    /** См. [PingSettingsRepository.save]. */
    suspend fun save(value: PingSettings) = settings.save(value)
}
