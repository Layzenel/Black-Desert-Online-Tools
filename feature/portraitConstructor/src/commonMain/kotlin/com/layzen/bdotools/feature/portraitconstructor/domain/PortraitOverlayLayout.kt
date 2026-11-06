package com.layzen.bdotools.feature.portraitconstructor.domain

/**
 * Где на портрете 624×804 рисуются надпись и значок, в пикселях текстуры.
 *
 * Окно выбора персонажа рисует поверх портрета свои элементы (ячейка в игре 100×128):
 * - верхняя полоса ~15 % высоты: значок группы слева и конь или якорь справа;
 * - нижняя полоса ~16 % высоты (от 108 / 128 ≈ 678 px): уровень, количество и две иконки по краям.
 *
 * В ячейке игры портрет уменьшается примерно в 6 раз, поэтому значок один и крупный — по центру портрета,
 * а надпись — над нижней полосой игры.
 */
object PortraitOverlayLayout {
    const val GAME_BOTTOM_STRIP_TOP = 678

    /**
     * Значок — 60 % ширины портрета по центру по горизонтали: мельче он в ячейке игры сливается в пятно.
     * По высоте он поднят так, чтобы не заходить на однострочную надпись и не задевать верхнюю полосу игры.
     */
    const val BADGE_SIZE = PortraitTexture.WIDTH * 6 / 10
    const val BADGE_CENTER_X = PortraitTexture.WIDTH / 2
    const val BADGE_CENTER_Y = 325

    /** Верхняя полоса игры (значок группы, конь, якорь) — около 15 % высоты. */
    const val GAME_TOP_STRIP_BOTTOM = PortraitTexture.HEIGHT * 15 / 100

    const val CAPTION_BOTTOM = GAME_BOTTOM_STRIP_TOP - 6

    /** Выше этой линии надпись не поднимается, даже двухстрочная. */
    const val CAPTION_MIN_TOP = 388
    const val CAPTION_PADDING_HORIZONTAL = 16
    const val CAPTION_PADDING_VERTICAL = 12

    /**
     * Шрифт уменьшается от наибольшего к наименьшему, пока надпись не влезет в две строки и по высоте
     * между [CAPTION_MIN_TOP] и [CAPTION_BOTTOM]. Короткая надпись получается крупной, в одну строку.
     */
    val CAPTION_FONT_SIZES = listOf(112, 104, 96, 88, 80, 74, 68, 62, 56)
    const val CAPTION_LINE_HEIGHT_RATIO = 1.08f

    /** Затемнение под надписью — овал, который темнее в центре и растворяется к краям. */
    const val CAPTION_GLOW_EXTRA_X = 40
    const val CAPTION_GLOW_EXTRA_Y = 36
}
