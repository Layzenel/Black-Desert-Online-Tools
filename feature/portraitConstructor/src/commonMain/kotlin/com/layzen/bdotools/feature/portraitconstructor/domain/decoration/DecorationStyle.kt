package com.layzen.bdotools.feature.portraitconstructor.domain.decoration

/**
 * Как нарисованы надпись и значок портрета. Цвета — `0xRRGGBB`, `null` — по умолчанию.
 *
 * @property captionColor цвет надписи; по умолчанию кремовый, как текст игры
 * @property badgeColor цвет значка (иконки)
 * @property shadeColor цвет затенения под надписью и значком; по умолчанию почти чёрный
 */
data class DecorationStyle(
    val captionColor: Int? = null,
    val badgeColor: Int? = null,
    val shadeColor: Int? = null,
)
