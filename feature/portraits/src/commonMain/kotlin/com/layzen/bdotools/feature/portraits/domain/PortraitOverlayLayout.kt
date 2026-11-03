package com.layzen.bdotools.feature.portraits.domain

/**
 * Где на портрете 624×804 рисуются надпись и значки, в пикселях текстуры.
 *
 * Окно выбора персонажа рисует поверх портрета свои элементы (ячейка в игре 100×128):
 * - верхняя полоса ~15 % высоты: значок группы слева и конь или якорь справа, середина свободна;
 * - нижняя полоса ~16 % высоты (от 108 / 128 ≈ 678 px): уровень, количество и две иконки по краям.
 *
 * Поэтому значок «сверху» стоит по центру верхней полосы, боковые — у краёв с заметным отступом,
 * а надпись — между боковыми значками и нижней полосой игры.
 */
object PortraitOverlayLayout {
    const val GAME_BOTTOM_STRIP_TOP = 678

    const val BADGE_DIAMETER = 152
    const val BADGE_ICON_SIZE = 102
    const val BADGE_RING_WIDTH = 6
    private const val BADGE_EDGE_MARGIN = 40
    private const val BADGE_TOP_MARGIN = 22
    private const val BADGE_SIDE_CENTER_Y = 300

    const val CAPTION_BOTTOM = GAME_BOTTOM_STRIP_TOP - 6

    /** Надпись не поднимается выше боковых значков. */
    const val CAPTION_MIN_TOP = BADGE_SIDE_CENTER_Y + BADGE_DIAMETER / 2 + 12
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

    fun badgeCenterX(slot: BadgeSlot): Int = when (slot) {
        BadgeSlot.Top -> PortraitTexture.WIDTH / 2
        BadgeSlot.Left -> BADGE_DIAMETER / 2 + BADGE_EDGE_MARGIN
        BadgeSlot.Right -> PortraitTexture.WIDTH - BADGE_DIAMETER / 2 - BADGE_EDGE_MARGIN
    }

    fun badgeCenterY(slot: BadgeSlot): Int = when (slot) {
        BadgeSlot.Top -> BADGE_DIAMETER / 2 + BADGE_TOP_MARGIN
        BadgeSlot.Left, BadgeSlot.Right -> BADGE_SIDE_CENTER_Y
    }
}
