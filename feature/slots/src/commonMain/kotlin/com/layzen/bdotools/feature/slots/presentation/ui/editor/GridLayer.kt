package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.presentation.state.GridStep
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Сетка от прицела: линии идут от осей через прицел в обе стороны, поэтому прицел всегда лежит на узле.
 * [scale] — пикселей страницы на один UI-px игры.
 */
@Composable
internal fun GridLayer(step: GridStep, screen: ScreenSpace, scale: Float, modifier: Modifier = Modifier) {
    val line = AppTheme.colors.textPrimary
    val axis = AppTheme.colors.accentGold
    Canvas(modifier) {
        val origin = crosshairOffset(screen, scale)
        val width = SlotsDimens.gridLine.toPx()
        if (step.size > 0) {
            val majorEvery = if (step == GridStep.Game) SlotsDimens.MAJOR_EVERY_COARSE else SlotsDimens.MAJOR_EVERY_FINE
            val spacing = step.size * scale
            val reach = maxOf(origin.x, size.width - origin.x, origin.y, size.height - origin.y)
            var k = 1
            while (k * spacing <= reach) {
                val alpha = if (k % majorEvery == 0) SlotsDimens.GRID_MAJOR_ALPHA else SlotsDimens.GRID_ALPHA
                drawCross(origin, k * spacing, line.copy(alpha = alpha), width)
                k++
            }
        }
        drawAxes(origin, axis.copy(alpha = SlotsDimens.AXIS_ALPHA), width)
    }
}

/** Прицел в пикселях холста. */
internal fun DrawScope.crosshairOffset(screen: ScreenSpace, scale: Float) =
    Offset(size.width / 2 + screen.crosshair.x * scale, size.height / 2 + screen.crosshair.y * scale)

/** Четыре линии на расстоянии [offset] от осей. */
private fun DrawScope.drawCross(origin: Offset, offset: Float, color: Color, width: Float) {
    for (x in listOf(origin.x - offset, origin.x + offset)) drawLine(color, Offset(x, 0f), Offset(x, size.height), width)
    for (y in listOf(origin.y - offset, origin.y + offset)) drawLine(color, Offset(0f, y), Offset(size.width, y), width)
}

private fun DrawScope.drawAxes(origin: Offset, color: Color, width: Float) {
    drawLine(color, Offset(origin.x, 0f), Offset(origin.x, size.height), width)
    drawLine(color, Offset(0f, origin.y), Offset(size.width, origin.y), width)
}
