package com.layzen.bdotools.feature.slots.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.uikit.theme.AppTheme

/** Подложка на весь экран игры, затенённая на [dimPercent] %: на тёмной картинке рамки слотов виднее. */
@Composable
internal fun BackgroundImage(image: ImageBitmap, dimPercent: Int) {
    Image(image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    if (dimPercent > 0) {
        Box(Modifier.fillMaxSize().background(AppTheme.colors.track.copy(alpha = dimPercent / 100f)))
    }
}
