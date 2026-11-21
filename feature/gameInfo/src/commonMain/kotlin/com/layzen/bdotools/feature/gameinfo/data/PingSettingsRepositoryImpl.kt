package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.fps.FpsStyle
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettingsRepository

/**
 * Настройки пинга строкой «показывать,размер,непрозрачность,FPS,вид FPS» (`true,100,80,false,Compact`). Испорченная строка читается
 * как настройки по умолчанию; FPS и его вид появились позже — строка без них читается с выключенным компактным FPS.
 *
 * @property storage где лежит строка
 */
class PingSettingsRepositoryImpl(private val storage: PingSettingsStorage) : PingSettingsRepository {
    override suspend fun settings(): PingSettings {
        val parts = storage.read()?.split(SEPARATOR) ?: return PingSettings()
        val shown = parts.getOrNull(0)?.toBooleanStrictOrNull() ?: return PingSettings()
        val scale = parts.getOrNull(1)?.toIntOrNull() ?: return PingSettings()
        val opacity = parts.getOrNull(2)?.toIntOrNull() ?: return PingSettings()
        val fpsShown = parts.getOrNull(3)?.toBooleanStrictOrNull() ?: false
        val fpsStyle = FpsStyle.entries.firstOrNull { it.name == parts.getOrNull(4) } ?: FpsStyle.Compact
        return PingSettings(shown, scale, opacity, fpsShown, fpsStyle)
    }

    override suspend fun save(settings: PingSettings) =
        storage.write(listOf(settings.shown, settings.scale, settings.opacity, settings.fpsShown, settings.fpsStyle.name).joinToString(SEPARATOR))

    private companion object {
        const val SEPARATOR = ","
    }
}
