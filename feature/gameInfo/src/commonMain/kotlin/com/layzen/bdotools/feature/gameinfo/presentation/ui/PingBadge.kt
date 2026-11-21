package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingQuality
import com.layzen.bdotools.feature.gameinfo.domain.ping.pingQuality
import com.layzen.bdotools.feature.gameinfo.resources.Res
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_value
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_unknown
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_ms
import com.layzen.bdotools.feature.gameinfo.resources.game_info_ping_unknown
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Пинг игры на тёмной подложке: значок связи и миллисекунды, оба цвета качества — до 60 мс зелёный, до 120
 * золотой, до 200 оранжевый, дальше красный; `null` — пинг не узнать (игра не запущена). Справа — [fps], если его
 * показывают; без пинга ([showPing] = `false`) на подложке один FPS.
 */
@Composable
internal fun PingBadge(ping: Int?, showPing: Boolean, fps: Int?, showFps: Boolean) {
    Row(
        Modifier.background(AppTheme.colors.backgroundDeep.copy(alpha = BACKDROP_ALPHA)).border(GameInfoDimens.frame, AppTheme.colors.border)
            .padding(horizontal = AppTheme.dimens.spaceS, vertical = AppTheme.dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPing) PingValue(ping)
        if (showFps) {
            Text(
                if (fps != null) stringResource(Res.string.game_info_fps_value, fps) else stringResource(Res.string.game_info_fps_unknown),
                style = AppTheme.typography.sectionTitle,
                color = if (fps != null) AppTheme.colors.textPrimary else AppTheme.colors.textMuted,
            )
        }
    }
}

/** Значок связи и миллисекунды цвета качества пинга. */
@Composable
private fun PingValue(ping: Int?) {
    val quality = pingQuality(ping)
    val color = qualityColor(quality)
    PingIcon(quality, color)
    Text(
        if (ping != null) stringResource(Res.string.game_info_ping_ms, ping) else stringResource(Res.string.game_info_ping_unknown),
        style = AppTheme.typography.sectionTitle,
        color = color,
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
