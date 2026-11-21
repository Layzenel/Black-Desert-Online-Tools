package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.layzen.bdotools.feature.gameinfo.domain.fps.FrameStats
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * График времени кадра за последние секунды: золотая линия по кадрам слева направо и пунктир среднего. Шкала — от
 * нуля до самого долгого кадра, но не ниже [FrameStats.FREEZE_FACTOR] средних: ровная игра — ровная линия внизу,
 * фриз — пик вверх.
 */
@Composable
internal fun FrameGraph(stats: FrameStats, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Canvas(modifier.fillMaxWidth().height(GameInfoDimens.frameGraphHeight)) {
        val times = stats.frameTimesMs
        val average = stats.averageMs
        val top = maxOf(stats.worstMs, average * FrameStats.FREEZE_FACTOR) * HEADROOM
        fun y(ms: Double) = (size.height * (1 - ms / top)).toFloat()
        val stroke = GameInfoDimens.frameGraphLine.toPx()
        drawLine(
            colors.textDisabled, Offset(0f, y(average)), Offset(size.width, y(average)), stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * DASH, stroke * DASH)),
        )
        val step = size.width / (times.size - 1).coerceAtLeast(1)
        val line = Path()
        times.forEachIndexed { index, ms ->
            if (index == 0) line.moveTo(0f, y(ms)) else line.lineTo(index * step, y(ms))
        }
        drawPath(line, colors.accentGold, style = Stroke(stroke))
    }
}

/** Запас сверху, чтобы пик фриза не упирался в край. */
private const val HEADROOM = 1.1

/** Длина штриха и промежутка пунктира — в толщинах линии. */
private const val DASH = 3f
