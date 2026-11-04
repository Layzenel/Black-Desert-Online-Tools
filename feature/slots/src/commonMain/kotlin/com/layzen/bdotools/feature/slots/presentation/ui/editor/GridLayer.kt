package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.layzen.bdotools.feature.slots.presentation.state.GridStep
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Сетка от центра экрана: линии идут от осей в обе стороны, поэтому центр всегда лежит на узле.
 * [scale] — пикселей страницы на один UI-px игры.
 */
@Composable
internal fun GridLayer(step: GridStep, scale: Float, modifier: Modifier = Modifier) {
    val line = AppTheme.colors.textPrimary
    val axis = AppTheme.colors.accentGold
    Canvas(modifier) {
        val width = SlotsDimens.gridLine.toPx()
        if (step.size > 0) {
            val majorEvery = if (step == GridStep.Game) SlotsDimens.MAJOR_EVERY_COARSE else SlotsDimens.MAJOR_EVERY_FINE
            val spacing = step.size * scale
            var k = 1
            while (k * spacing <= maxOf(center.x, center.y)) {
                val alpha = if (k % majorEvery == 0) SlotsDimens.GRID_MAJOR_ALPHA else SlotsDimens.GRID_ALPHA
                drawCross(k * spacing, line.copy(alpha = alpha), width)
                k++
            }
        }
        drawAxes(axis.copy(alpha = SlotsDimens.AXIS_ALPHA), width)
    }
}

/** Четыре линии на расстоянии [offset] от осей. */
private fun DrawScope.drawCross(offset: Float, color: Color, width: Float) {
    for (x in listOf(center.x - offset, center.x + offset)) drawLine(color, Offset(x, 0f), Offset(x, size.height), width)
    for (y in listOf(center.y - offset, center.y + offset)) drawLine(color, Offset(0f, y), Offset(size.width, y), width)
}

private fun DrawScope.drawAxes(color: Color, width: Float) {
    drawLine(color, Offset(center.x, 0f), Offset(center.x, size.height), width)
    drawLine(color, Offset(0f, center.y), Offset(size.width, center.y), width)
}
