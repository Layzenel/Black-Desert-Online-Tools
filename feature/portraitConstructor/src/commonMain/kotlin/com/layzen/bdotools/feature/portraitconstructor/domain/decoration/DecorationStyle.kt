package com.layzen.bdotools.feature.portraitconstructor.domain.decoration

/**
 * Как нарисованы надпись и значок портрета. Цвета — `0xRRGGBB`, `null` — по умолчанию.
 *
 * @property captionColor цвет надписи; по умолчанию кремовый, как текст игры
 * @property badgeColor цвет значка (иконки)
 * @property shadeColor цвет затенения под надписью и значком; по умолчанию почти чёрный
 * @property shadeStrength сила затенения в процентах ([SHADE_RANGE]); `null` — [DEFAULT_SHADE_STRENGTH]
 * @property shadeSize область затенения — насколько тень выступает за надпись и значок, в процентах
 * ([SHADE_SIZE_RANGE]); `null` — [DEFAULT_SHADE_SIZE]
 */
data class DecorationStyle(
    val captionColor: Int? = null,
    val badgeColor: Int? = null,
    val shadeColor: Int? = null,
    val shadeStrength: Int? = null,
    val shadeSize: Int? = null,
) {
    /** Область затенения как множитель для отрисовки: 1 — как по умолчанию. */
    val shadeSizeFactor: Float get() = (shadeSize ?: DEFAULT_SHADE_SIZE).coerceIn(SHADE_SIZE_RANGE) / DEFAULT_SHADE_SIZE.toFloat()

    /** Сила затенения от 0 до 1 для отрисовки. */
    val shadeFactor: Float get() = (shadeStrength ?: DEFAULT_SHADE_STRENGTH).coerceIn(SHADE_RANGE) / DEFAULT_SHADE_STRENGTH.toFloat()

    companion object {
        /** Сила затенения как до появления настройки: прежние портреты рисуются так же. */
        const val DEFAULT_SHADE_STRENGTH = 100

        /** Допустимая сила затенения, %: 0 — без затенения. */
        val SHADE_RANGE = 0..DEFAULT_SHADE_STRENGTH

        /** Область затенения по умолчанию, %. */
        const val DEFAULT_SHADE_SIZE = 100

        /** Допустимая область затенения, %: 0 — тень вплотную к контуру. */
        val SHADE_SIZE_RANGE = 0..300
    }
}
