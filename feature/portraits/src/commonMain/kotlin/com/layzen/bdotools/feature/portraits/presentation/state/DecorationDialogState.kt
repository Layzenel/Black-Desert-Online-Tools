package com.layzen.bdotools.feature.portraits.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraits.domain.PortraitDecoration

/** Окно надписи или значков. [base] — портрет без украшений, `null` пока загружается. */
data class DecorationDialogState(
    val characterId: String,
    val mode: DecorationMode,
    val draft: PortraitDecoration,
    val base: ImageBitmap? = null,
)
