package com.layzen.bdotools.feature.portraits.presentation.state

import androidx.compose.ui.graphics.ImageBitmap

/** Портрет персонажа на странице. [image] `null`, пока портрет читается: на его месте скелетон. */
class PortraitItem(val characterId: String, val image: ImageBitmap?)
