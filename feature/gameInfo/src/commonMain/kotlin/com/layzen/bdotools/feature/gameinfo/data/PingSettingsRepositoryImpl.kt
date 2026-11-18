package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettingsRepository

/**
 * Настройки пинга строкой «показывать,размер,непрозрачность» (`true,100,80`). Испорченная строка читается как
 * настройки по умолчанию.
 *
 * @property storage где лежит строка
 */
class PingSettingsRepositoryImpl(private val storage: PingSettingsStorage) : PingSettingsRepository {
    override suspend fun settings(): PingSettings {
        val parts = storage.read()?.split(SEPARATOR) ?: return PingSettings()
        val shown = parts.getOrNull(0)?.toBooleanStrictOrNull() ?: return PingSettings()
        val scale = parts.getOrNull(1)?.toIntOrNull() ?: return PingSettings()
        val opacity = parts.getOrNull(2)?.toIntOrNull() ?: return PingSettings()
        return PingSettings(shown, scale, opacity)
    }

    override suspend fun save(settings: PingSettings) =
        storage.write(listOf(settings.shown, settings.scale, settings.opacity).joinToString(SEPARATOR))

    private companion object {
        const val SEPARATOR = ","
    }
}
