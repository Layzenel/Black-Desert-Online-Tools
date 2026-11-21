package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.gameinfo.domain.fps.FpsStyle
import com.layzen.bdotools.feature.gameinfo.domain.fps.FrameStats
import com.layzen.bdotools.feature.gameinfo.resources.Res
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_freezes
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_low
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_unknown
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_value
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Числа FPS в строке информационной панели: FPS и «1% low», в подробном виде ([style]) ещё число фризов — оранжевым,
 * если они были. [stats] `null` — «— FPS».
 */
@Composable
internal fun FpsValue(stats: FrameStats?, style: FpsStyle) {
    val colors = AppTheme.colors
    val textStyle = AppTheme.typography.sectionTitle
    if (stats == null) {
        Text(stringResource(Res.string.game_info_fps_unknown), style = textStyle, color = colors.textMuted)
        return
    }
    Text(stringResource(Res.string.game_info_fps_value, stats.fps), style = textStyle, color = colors.textPrimary)
    Text(stringResource(Res.string.game_info_fps_low, stats.lowFps), style = textStyle, color = colors.textMuted)
    if (style == FpsStyle.Detailed) {
        Text(
            stringResource(Res.string.game_info_fps_freezes, stats.freezes),
            style = textStyle,
            color = if (stats.freezes > 0) colors.textNote else colors.textMuted,
        )
    }
}
