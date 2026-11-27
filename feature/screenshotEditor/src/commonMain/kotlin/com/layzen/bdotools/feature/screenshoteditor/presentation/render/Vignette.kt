package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.ImageRect
import kotlin.math.hypot

// Виньетка 100 затемняет углы на 85 %; середина (до 45 % радиуса) не темнеет
private const val MAX_DARKNESS = 0.85f
private const val CLEAR_CENTER = 0.45f

/** Затемнение к краям [frame] силой [strength] (0–100). */
internal fun DrawScope.vignette(frame: ImageRect, strength: Int) {
    val center = Offset((frame.left + frame.right) / 2, (frame.top + frame.bottom) / 2)
    val radius = hypot(frame.width, frame.height) / 2
    val brush = Brush.radialGradient(
        CLEAR_CENTER to Color.Transparent,
        1f to Color.Black.copy(alpha = MAX_DARKNESS * strength / 100f),
        center = center,
        radius = radius,
    )
    drawRect(brush, Offset(frame.left, frame.top), Size(frame.width, frame.height))
}
