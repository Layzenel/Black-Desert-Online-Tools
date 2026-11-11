package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Иконка навыка размером [size]; пока она грузится — тёмный квадрат того же размера. */
@Composable
internal fun SkillIcon(image: ImageBitmap?, size: Dp = SlotsDimens.skillIcon) {
    Box(Modifier.size(size).background(AppTheme.colors.backgroundInput)) {
        image?.let { Image(it, contentDescription = null, Modifier.matchParentSize(), contentScale = ContentScale.Crop) }
    }
}
