package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Половина диагонали ромба прицела, UI-px: замерено по скриншоту игры. */
private const val CROSSHAIR_RADIUS = 27f

/** Какая доля стороны ромба нарисована у каждого угла: в игре стороны ромба разорваны посередине. */
private const val CHEVRON_SHARE = 0.4f

/** Длина лучей крестика в центре — доля [CROSSHAIR_RADIUS]. */
private const val CROSS_SHARE = 0.3f

/** Прицел игры: ромб из четырёх уголков и маленький крестик в центре. Масштабируется вместе с экраном. */
@Composable
internal fun CrosshairLayer(screen: ScreenSpace, scale: Float, modifier: Modifier = Modifier) {
    val color = AppTheme.colors.textPrimary.copy(alpha = SlotsDimens.CROSSHAIR_ALPHA)
    Canvas(modifier) {
        val center = crosshairOffset(screen, scale)
        val radius = CROSSHAIR_RADIUS * scale
        val width = SlotsDimens.crosshairStroke.toPx()
        val corners = listOf(Offset(0f, -radius), Offset(radius, 0f), Offset(0f, radius), Offset(-radius, 0f))
        corners.forEachIndexed { i, corner ->
            val previous = corners[(i + 3) % 4]
            val next = corners[(i + 1) % 4]
            drawChevron(center + corner, (previous - corner) * CHEVRON_SHARE, (next - corner) * CHEVRON_SHARE, color, width)
        }
        val arm = radius * CROSS_SHARE
        drawLine(color, center + Offset(-arm, 0f), center + Offset(arm, 0f), width)
        drawLine(color, center + Offset(0f, -arm), center + Offset(0f, arm), width)
    }
}

private fun DrawScope.drawChevron(tip: Offset, toPrevious: Offset, toNext: Offset, color: Color, width: Float) {
    drawLine(color, tip, tip + toPrevious, width)
    drawLine(color, tip, tip + toNext, width)
}
