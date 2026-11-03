package com.layzen.bdotools.uikit.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.layzen.bdotools.uikit.theme.AppShapes
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

/** Строка «крутилка + текст» для небольших ожиданий внутри окна. */
@Composable
fun LoadingRow(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
        Spinner(size = AppTheme.dimens.iconSize)
        DescriptionText(text)
    }
}

/**
 * Полоса прогресса: дорожка как у слайдера и золотая заливка. [progress] от 0 до 1.
 */
@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(AppTheme.dimens.progressHeight)
            .background(colors.track, AppShapes.Pill),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(AppTheme.dimens.progressHeight)
                .background(colors.accentGold, AppShapes.Pill),
        )
    }
}

/**
 * Окно ожидания поверх страницы: закрыть нельзя, пока идёт долгая операция.
 * [progress] — доля выполненного или `null`, если её не посчитать.
 */
@Composable
fun BusyOverlay(text: String, progress: Float? = null, details: String? = null) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier
                .width(AppTheme.dimens.busyOverlayWidth)
                .background(AppTheme.colors.backgroundRaised)
                .border(AppTheme.dimens.borderWidth, AppTheme.colors.borderSelected)
                .padding(AppTheme.dimens.spaceL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceM),
        ) {
            Spinner()
            Text(text, style = AppTheme.typography.sectionTitle, color = AppTheme.colors.textBeige)
            if (progress != null) ProgressBar(progress)
            if (details != null) DescriptionText(details)
        }
    }
}
