package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.feature.gameinfo.domain.fps.FpsStyle
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettings
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettingsRepository

/**
 * Настройки строкой «показывать,размер,непрозрачность,FPS,вид FPS,сетка,шаг сетки,непрозрачность сетки,время» (`true,100,80,false,Compact,false,Medium,100,false`).
 * Испорченная строка читается как настройки по умолчанию; FPS, сетка, их виды и время появились позже — строка без них
 * читается с выключенными FPS, сеткой и временем.
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
        val gridShown = parts.getOrNull(5)?.toBooleanStrictOrNull() ?: false
        val gridStep = GridStep.entries.firstOrNull { it.name == parts.getOrNull(6) } ?: GridStep.Medium
        val gridOpacity = parts.getOrNull(7)?.toIntOrNull() ?: PingSettings().gridOpacity
        val timeShown = parts.getOrNull(8)?.toBooleanStrictOrNull() ?: false
        return PingSettings(shown, scale, opacity, fpsShown, fpsStyle, gridShown, gridStep, gridOpacity, timeShown)
    }

    override suspend fun save(settings: PingSettings) = storage.write(
        listOf(
            settings.shown, settings.scale, settings.opacity, settings.fpsShown, settings.fpsStyle.name,
            settings.gridShown, settings.gridStep.name, settings.gridOpacity, settings.timeShown,
        ).joinToString(SEPARATOR),
    )

    private companion object {
        const val SEPARATOR = ","
    }
}
