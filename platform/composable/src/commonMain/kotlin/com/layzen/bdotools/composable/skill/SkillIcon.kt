package com.layzen.bdotools.composable.skill

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.components.Skeleton
import com.layzen.bdotools.uikit.theme.AppTheme

/** Иконка навыка размером [size]; пока она грузится — пульсирующая заглушка [Skeleton] того же размера. */
@Composable
fun SkillIcon(image: ImageBitmap?, size: Dp) {
    Box(Modifier.size(size).background(AppTheme.colors.backgroundInput)) {
        if (image == null) Skeleton(Modifier.matchParentSize())
        else Image(image, contentDescription = null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
    }
}
