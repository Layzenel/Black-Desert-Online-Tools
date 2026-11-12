package com.layzen.bdotools.feature.portraitconstructor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.DecorationStyle
import com.layzen.bdotools.feature.portraitconstructor.domain.decoration.PortraitDecoration

/**
 * Окно «Цвет заметок, значков и затенения» всех портретов: выбранные цвета и затенение [style].
 * Превью — первый портрет с заметкой или значком: его украшение [previewDecoration] и чистая основа [previewBase]
 * (`null`, пока читается или если украшенных портретов нет).
 */
data class DecorationColorsState(
    val style: DecorationStyle = DecorationStyle(),
    val previewDecoration: PortraitDecoration? = null,
    val previewBase: ImageBitmap? = null,
)
