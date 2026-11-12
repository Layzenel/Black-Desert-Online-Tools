package com.layzen.bdotools.feature.portraitconstructor.presentation.state

/** Окно «Цвет заметок и значков» всех портретов: выбранные цвета `0xRRGGBB`, `null` — цвет по умолчанию. */
data class DecorationColorsState(
    val captionColor: Int? = null,
    val badgeColor: Int? = null,
)
