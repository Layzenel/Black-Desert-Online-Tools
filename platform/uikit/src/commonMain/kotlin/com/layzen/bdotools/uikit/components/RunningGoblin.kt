package com.layzen.bdotools.uikit.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppTheme

private const val STRIDE_DURATION_MS = 600

/** Индикатор загрузки: чёрный гоблин со светлой обводкой бежит на месте. */
@Composable
fun RunningGoblin(modifier: Modifier = Modifier, size: Dp = AppTheme.dimens.loaderSize) {
    val colors = AppTheme.colors
    val outline = AppTheme.dimens.loaderOutline
    val phase by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(STRIDE_DURATION_MS, easing = LinearEasing), RepeatMode.Restart),
    )
    Canvas(modifier.size(size)) {
        val pose = goblinPose(phase)
        drawGoblin(pose, colors.silhouetteOutline, grow = outline.toPx())
        drawGoblin(pose, colors.silhouette, grow = 0f)
    }
}
