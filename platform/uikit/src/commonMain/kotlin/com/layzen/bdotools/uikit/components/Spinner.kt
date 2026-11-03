package com.layzen.bdotools.uikit.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppTheme

private const val SPIN_DURATION_MS = 900
private const val FULL_TURN = 360f
private const val ARC_SWEEP = 270f

/** Крутящееся золотое кольцо. */
@Composable
fun Spinner(modifier: Modifier = Modifier, size: Dp = AppTheme.dimens.spinnerSize) {
    val colors = AppTheme.colors
    val stroke = AppTheme.dimens.spinnerStroke
    val angle by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = FULL_TURN,
        animationSpec = infiniteRepeatable(tween(SPIN_DURATION_MS, easing = LinearEasing), RepeatMode.Restart),
    )
    Canvas(modifier.size(size)) {
        val width = stroke.toPx()
        drawCircle(colors.track, radius = (this.size.minDimension - width) / 2, style = Stroke(width))
        rotate(angle) {
            drawArc(
                colors.accentGold,
                startAngle = 0f,
                sweepAngle = ARC_SWEEP,
                useCenter = false,
                topLeft = Offset(width / 2, width / 2),
                size = Size(this.size.width - width, this.size.height - width),
                style = Stroke(width, cap = StrokeCap.Round),
            )
        }
    }
}
