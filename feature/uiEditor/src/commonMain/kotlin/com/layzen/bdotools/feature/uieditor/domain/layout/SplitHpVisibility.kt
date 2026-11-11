package com.layzen.bdotools.feature.uieditor.domain.layout

/** Здоровье, PVP и гнев Чёрного духа: при выключенном «Разделении HP» игра прячет их и собирает в [FixedElement.CombinedHp]. */
internal val SPLIT_HP_ELEMENTS = setOf(InterfaceElement.Health, InterfaceElement.Pvp, InterfaceElement.BlackSpiritRage)

/**
 * Скрытые элементы после переключения «Разделения HP» в [enabled], как это делает игра: включённое показывает
 * здоровье, PVP и гнев отдельно, выключенное прячет их.
 */
internal fun hiddenWithSplitHp(hidden: Set<SlotId>, enabled: Boolean): Set<SlotId> {
    val elements = SPLIT_HP_ELEMENTS.map { it.slotId }.toSet()
    return if (enabled) hidden - elements else hidden + elements
}
