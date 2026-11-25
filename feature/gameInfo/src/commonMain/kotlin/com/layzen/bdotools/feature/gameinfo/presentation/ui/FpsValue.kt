package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.gameinfo.domain.fps.FpsStyle
import com.layzen.bdotools.feature.gameinfo.domain.fps.FrameStats
import com.layzen.bdotools.feature.gameinfo.resources.Res
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_freezes
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_low
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_low_unknown
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_unknown
import com.layzen.bdotools.feature.gameinfo.resources.game_info_fps_value
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Числа FPS в строке информационной панели: FPS и «1% low» (ширина — под трёхзначное число, панель не прыгает), в
 * подробном виде ([style]) ещё число фризов — оранжевым, если они были. [stats] `null` — «— FPS» и «1% —».
 */
@Composable
internal fun FpsValue(stats: FrameStats?, style: FpsStyle) {
    val colors = AppTheme.colors
    val textStyle = AppTheme.typography.sectionTitle
    FixedWidthText(
        if (stats != null) stringResource(Res.string.game_info_fps_value, stats.fps) else stringResource(Res.string.game_info_fps_unknown),
        stringResource(Res.string.game_info_fps_value, WIDEST), textStyle,
        if (stats != null) colors.textPrimary else colors.textMuted,
    )
    FixedWidthText(
        if (stats != null) stringResource(Res.string.game_info_fps_low, stats.lowFps) else stringResource(Res.string.game_info_fps_low_unknown),
        stringResource(Res.string.game_info_fps_low, WIDEST), textStyle, colors.textMuted,
    )
    if (style == FpsStyle.Detailed && stats != null) {
        Text(
            stringResource(Res.string.game_info_fps_freezes, stats.freezes),
            style = textStyle,
            color = if (stats.freezes > 0) colors.textNote else colors.textMuted,
        )
    }
}

/** Самое широкое число, под которое резервируется место. */
private const val WIDEST = 999
