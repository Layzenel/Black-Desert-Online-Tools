package com.layzen.bdotools.feature.portraits.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraits.domain.ImagePlacement
import com.layzen.bdotools.feature.portraits.domain.PortraitGrid

/** Подгонка картинки под маску: картинка, сетка ячеек и её текущее положение. */
data class EditorState(
    val target: ReplaceTarget,
    val image: ImageBitmap,
    val grid: PortraitGrid,
    val initialPlacement: ImagePlacement,
    val placement: ImagePlacement = initialPlacement,
)
