package com.layzen.bdotools.feature.portraitconstructor.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter

/** Рисует SVG-иконку [size]×[size] с центром в [center], перекрашенную в [color]. */
internal fun DrawScope.drawIcon(painter: Painter, center: Offset, size: Float, color: Color) {
    translate(center.x - size / 2, center.y - size / 2) {
        with(painter) { draw(Size(size, size), colorFilter = ColorFilter.tint(color)) }
    }
}
