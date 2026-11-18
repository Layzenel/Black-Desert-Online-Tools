package com.layzen.bdotools.composable.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * [content] размером [scalePercent] и непрозрачностью [opacityPercent] (проценты из настроек). Размер — через
 * плотность: содержимое и окно под ним растут целиком, а не растягиваются картинкой.
 */
@Composable
fun ScaledOverlay(scalePercent: Int, opacityPercent: Int, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density * scalePercent / PERCENT, density.fontScale)) {
        Box(Modifier.alpha(opacityPercent / PERCENT)) { content() }
    }
}

private const val PERCENT = 100f
