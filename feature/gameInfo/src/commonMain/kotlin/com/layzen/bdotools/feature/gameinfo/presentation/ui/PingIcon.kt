package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingQuality

/**
 * Значок связи: три столбика по возрастанию, как у сигнала сети. Закрашено столько, насколько хорош пинг [quality]
 * (хороший — три, заметная задержка — два, хуже — один), цветом [color]; остальные — бледные.
 */
@Composable
internal fun PingIcon(quality: PingQuality?, color: Color, modifier: Modifier = Modifier) {
    val filled = when (quality) {
        PingQuality.Good -> BARS
        PingQuality.Fair -> BARS - 1
        PingQuality.Poor, PingQuality.Bad -> 1
        null -> 0
    }
    Canvas(modifier.size(GameInfoDimens.pingIcon)) {
        val gap = GameInfoDimens.pingBarGap.toPx()
        val width = (size.width - gap * (BARS - 1)) / BARS
        for (bar in 0 until BARS) {
            val height = size.height * (bar + 1) / BARS
            drawRoundRect(
                color = if (bar < filled) color else color.copy(alpha = EMPTY_ALPHA),
                topLeft = Offset(bar * (width + gap), size.height - height),
                size = Size(width, height),
                cornerRadius = CornerRadius(width / 3),
            )
        }
    }
}

private const val BARS = 3
private const val EMPTY_ALPHA = 0.25f
