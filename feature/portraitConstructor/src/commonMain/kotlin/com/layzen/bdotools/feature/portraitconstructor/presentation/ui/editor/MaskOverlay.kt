package com.layzen.bdotools.feature.portraitconstructor.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.layzen.bdotools.feature.portraitconstructor.domain.PortraitGrid
import com.layzen.bdotools.feature.portraitconstructor.presentation.ui.PortraitsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

private const val MASK_SCRIM_ALPHA = 0.7f

/**
 * Затемнение вокруг ячеек маски с рамками ячеек. [k] — пикселей экрана на пиксель маски.
 * Выключенные ячейки [disabledCells] остаются затемнёнными, с тусклой рамкой и крестом.
 */
@Composable
internal fun MaskOverlay(grid: PortraitGrid, disabledCells: Set<Int>, margin: Float, k: Float, modifier: Modifier) {
    val scrim = AppTheme.colors.backgroundDeep.copy(alpha = MASK_SCRIM_ALPHA)
    val border = AppTheme.colors.accentGoldPale
    val disabledBorder = AppTheme.colors.textDisabled
    val borderWidth = with(LocalDensity.current) { PortraitsDimens.maskBorderWidth.toPx() }
    Canvas(modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
        drawRect(scrim)
        (0 until grid.count).forEach { index ->
            val cell = grid.cell(index)
            val topLeft = Offset((cell.x + margin) * k, (cell.y + margin) * k)
            val size = Size(cell.width * k, cell.height * k)
            if (index in disabledCells) {
                drawRect(disabledBorder, topLeft, size, style = Stroke(borderWidth))
                val bottomRight = topLeft + Offset(size.width, size.height)
                drawLine(disabledBorder, topLeft, bottomRight, borderWidth)
                drawLine(disabledBorder, Offset(bottomRight.x, topLeft.y), Offset(topLeft.x, bottomRight.y), borderWidth)
            } else {
                drawRect(Color.Transparent, topLeft, size, blendMode = BlendMode.Clear)
                drawRect(border, topLeft, size, style = Stroke(borderWidth))
            }
        }
    }
}
