package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.layzen.bdotools.feature.slots.domain.ScreenSpace
import com.layzen.bdotools.feature.slots.domain.SnapGuides
import com.layzen.bdotools.feature.slots.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Золотые линии на всю ширину и высоту экрана игры: к чему прилип двигаемый элемент. Координаты линий — от центра экрана. */
@Composable
internal fun SnapGuidesLayer(guides: SnapGuides, screen: ScreenSpace, scale: Float, modifier: Modifier = Modifier) {
    val color = AppTheme.colors.accentGold
    Canvas(modifier) {
        val width = SlotsDimens.guideStroke.toPx()
        guides.x?.let { x ->
            val px = ((screen.width / 2 + x) * scale).toFloat()
            drawLine(color, Offset(px, 0f), Offset(px, size.height), width)
        }
        guides.y?.let { y ->
            val py = ((screen.height / 2 + y) * scale).toFloat()
            drawLine(color, Offset(0f, py), Offset(size.width, py), width)
        }
    }
}
