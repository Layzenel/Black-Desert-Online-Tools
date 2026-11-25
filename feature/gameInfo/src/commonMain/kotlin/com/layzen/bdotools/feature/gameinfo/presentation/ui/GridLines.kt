package com.layzen.bdotools.feature.gameinfo.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.layzen.bdotools.feature.gameinfo.domain.grid.GridStep
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Сетка на весь экран игры от его центра, как в редакторе интерфейса: линии идут от осей через центр в обе стороны,
 * оси золотые, каждая [GameInfoDimens.MAJOR_EVERY_FINE]-я (у шага [GridStep.Game] — вторая) заметнее. Шаг задан в UI-px
 * игры; [uiHeight] — высота экрана в UI-px, по ней шаг переводится в пиксели холста.
 */
@Composable
internal fun GridLines(step: GridStep, opacity: Int, uiHeight: Double) {
    // ползунок 20–100 % растянут на всю альфу: на минимуме сетка совсем прозрачная, на максимуме — во всю силу.
    // Множитель входит в альфу каждой линии: прежде её подменяла постоянная, и ползунок ничего не менял
    val fade = ((opacity - OPACITY.first).toFloat() / (OPACITY.last - OPACITY.first)).coerceIn(0f, 1f)
    val line = AppTheme.colors.textPrimary
    val axis = AppTheme.colors.accentGold
    Canvas(Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val width = GameInfoDimens.gridLine.toPx()
        val spacing = (step.size * size.height / uiHeight).toFloat()
        if (spacing >= width) {
            val majorEvery = if (step == GridStep.Game) GameInfoDimens.MAJOR_EVERY_COARSE else GameInfoDimens.MAJOR_EVERY_FINE
            val reach = maxOf(center.x, center.y)
            var k = 1
            while (k * spacing <= reach) {
                val alpha = if (k % majorEvery == 0) GameInfoDimens.GRID_MAJOR_ALPHA else GameInfoDimens.GRID_ALPHA
                drawCross(center, k * spacing, line.copy(alpha = alpha * fade), width)
                k++
            }
        }
        drawLine(axis.copy(alpha = GameInfoDimens.GRID_AXIS_ALPHA * fade), Offset(center.x, 0f), Offset(center.x, size.height), width)
        drawLine(axis.copy(alpha = GameInfoDimens.GRID_AXIS_ALPHA * fade), Offset(0f, center.y), Offset(size.width, center.y), width)
    }
}

/** Четыре линии на расстоянии [offset] от осей через [origin]. */
private fun DrawScope.drawCross(origin: Offset, offset: Float, color: Color, width: Float) {
    for (x in listOf(origin.x - offset, origin.x + offset)) drawLine(color, Offset(x, 0f), Offset(x, size.height), width)
    for (y in listOf(origin.y - offset, origin.y + offset)) drawLine(color, Offset(0f, y), Offset(size.width, y), width)
}
