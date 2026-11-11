package com.layzen.bdotools.feature.uieditor.presentation.ui.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme

/** Маленький портрет персонажа из `FaceTexture`; пока он грузится или если файла нет — пустая ячейка. */
@Composable
internal fun CharacterPortrait(image: ImageBitmap?) {
    Box(Modifier.size(SlotsDimens.portraitWidth, SlotsDimens.portraitHeight).background(AppTheme.colors.backgroundInput)) {
        image?.let { Image(it, contentDescription = null, Modifier.matchParentSize(), contentScale = ContentScale.Crop) }
    }
}
