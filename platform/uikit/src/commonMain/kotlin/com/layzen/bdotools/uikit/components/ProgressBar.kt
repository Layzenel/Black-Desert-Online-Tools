package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

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
