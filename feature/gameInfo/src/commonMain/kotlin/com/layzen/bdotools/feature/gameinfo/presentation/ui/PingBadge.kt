package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.gameinfo.domain.fps.FpsStyle
import com.layzen.bdotools.feature.gameinfo.domain.fps.FrameStats
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingQuality
import com.layzen.bdotools.feature.gameinfo.domain.ping.pingQuality
import com.layzen.bdotools.feature.gameinfo.resources.Res
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_ms
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_unknown
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Пинг игры на тёмной подложке: значок связи и миллисекунды, оба цвета качества — до 60 мс зелёный, до 120
 * золотой, до 200 оранжевый, дальше красный; `null` — пинг не узнать (игра не запущена). Справа — FPS ([fps]), если его
 * показывают; без пинга ([showPing] = `false`) на подложке один FPS. В подробном виде ([fpsStyle]) под строкой —
 * график времени кадра шириной со строку.
 */
@Composable
internal fun PingBadge(ping: Int?, showPing: Boolean, fps: FrameStats?, showFps: Boolean, fpsStyle: FpsStyle) {
    Column(
        Modifier.width(IntrinsicSize.Max)
            .background(AppTheme.colors.backgroundDeep.copy(alpha = BACKDROP_ALPHA)).border(GameInfoDimens.frame, AppTheme.colors.border)
            .padding(horizontal = AppTheme.dimens.spaceS, vertical = AppTheme.dimens.spaceXs),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS), verticalAlignment = Alignment.CenterVertically) {
            if (showPing) PingValue(ping)
            if (showFps) FpsValue(fps, fpsStyle)
        }
        if (showFps && fpsStyle == FpsStyle.Detailed && fps != null) FrameGraph(fps)
    }
}

/** Значок связи и миллисекунды цвета качества пинга. */
@Composable
private fun PingValue(ping: Int?) {
    val quality = pingQuality(ping)
    val color = qualityColor(quality)
    PingIcon(quality, color)
    // ширина — под трёхзначный пинг, чтобы панель не прыгала
    FixedWidthText(
        if (ping != null) stringResource(Res.string.game_info_ping_ms, ping) else stringResource(Res.string.game_info_ping_unknown),
        stringResource(Res.string.game_info_ping_ms, WIDEST_PING), AppTheme.typography.sectionTitle, color,
    )
}

@Composable
private fun qualityColor(quality: PingQuality?): Color = when (quality) {
    PingQuality.Good -> AppTheme.colors.success
    PingQuality.Fair -> AppTheme.colors.accentGold
    PingQuality.Poor -> AppTheme.colors.textNote
    PingQuality.Bad -> AppTheme.colors.accentRed
    null -> AppTheme.colors.textMuted
}

private const val BACKDROP_ALPHA = 0.8f

/** Самый широкий пинг, под который резервируется место. */
private const val WIDEST_PING = 999
