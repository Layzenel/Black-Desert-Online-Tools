package com.layzen.bdotools.uikit.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.lerp
import com.layzen.bdotools.uikit.theme.AppTheme

private const val PULSE_MS = 900

/**
 * Заглушка на месте содержимого, которое ещё грузится: плавно пульсирует между цветами панели и пункта.
 * Размер задаёт [modifier] — обычно `matchParentSize()` внутри плитки или фиксированная ширина и высота строки.
 */
@Composable
fun Skeleton(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = LinearEasing), RepeatMode.Reverse),
    )
    Box(modifier.drawBehind { drawRect(lerp(colors.backgroundRaised, colors.backgroundItem, pulse)) })
}
