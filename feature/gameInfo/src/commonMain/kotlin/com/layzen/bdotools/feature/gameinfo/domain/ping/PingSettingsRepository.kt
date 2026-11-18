package com.layzen.bdotools.feature.gameinfo.domain.ping

/** Сохранённые настройки показа пинга. */
interface PingSettingsRepository {
    /** Настройки; по умолчанию, если их ещё не сохраняли. */
    suspend fun settings(): PingSettings

    /** Сохраняет [settings]. */
    suspend fun save(settings: PingSettings)
}
