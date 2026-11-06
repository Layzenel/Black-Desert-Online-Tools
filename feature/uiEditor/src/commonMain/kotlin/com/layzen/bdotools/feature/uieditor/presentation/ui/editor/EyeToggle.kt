package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Высота глаза — доля ширины значка. */
private const val EYE_HEIGHT_SHARE = 0.55f

/** Радиус зрачка — доля ширины значка. */
private const val PUPIL_SHARE = 0.14f

/**
 * Глазик видимости элемента, как в режиме правки интерфейса игры: светлый — элемент показан,
 * серый — скрыт. Клик переключает; нажатие поглощается, чтобы не схватить элемент под глазом.
 */
@Composable
internal fun EyeToggle(visible: Boolean, eyeSize: Dp, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val color = if (visible) AppTheme.colors.textPrimary else AppTheme.colors.textDisabled
    Canvas(modifier.size(eyeSize).consumingTap(onToggle)) {
        val eyeHeight = size.width * EYE_HEIGHT_SHARE
        val stroke = SlotsDimens.eyeStroke.toPx()
        drawOval(
            color,
            topLeft = Offset(0f, (size.height - eyeHeight) / 2),
            size = Size(size.width, eyeHeight),
            style = Stroke(stroke),
        )
        drawCircle(color, radius = size.width * PUPIL_SHARE, center = center)
    }
}
