package com.layzen.bdotools.feature.gameinfo.domain.ping

import com.layzen.bdotools.feature.gameinfo.domain.fps.FpsStyle
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep

/**
 * Как показывать сведения об игре поверх неё: пинг и FPS (одно окно) и сетку на весь экран.
 *
 * @property shown показывать пинг
 * @property fpsShown показывать FPS справа от пинга; действует, только пока программа запущена от администратора
 * @property fpsStyle как показывать FPS: одной строкой или подробно, с графиком
 * @property scale размер в процентах
 * @property opacity непрозрачность в процентах
 * @property gridShown показывать сетку на весь экран игры
 * @property gridStep шаг линий сетки
 * @property gridOpacity непрозрачность сетки в процентах
 */
data class PingSettings(
    val shown: Boolean = false,
    val scale: Int = 100,
    val opacity: Int = 100,
    val fpsShown: Boolean = false,
    val fpsStyle: FpsStyle = FpsStyle.Compact,
    val gridShown: Boolean = false,
    val gridStep: GridStep = GridStep.Medium,
    val gridOpacity: Int = 100,
)
