package com.layzen.bdotools.feature.uieditor.domain.layout

/**
 * Скрытые элементы после переключения «Настройки быстрых слотов» в [enabled], как это делает игра: включённая
 * показывает 20 слотов хотбара и прячет «Быстрый слот», выключенная — наоборот.
 */
internal fun hiddenWithCustomQuickSlots(hidden: Set<SlotId>, enabled: Boolean): Set<SlotId> {
    val hotbar = SlotGroup.Hotbar.slots.toSet()
    val quickSlot = setOf(InterfaceElement.QuickSlot.slotId)
    return if (enabled) hidden - hotbar + quickSlot else hidden - quickSlot + hotbar
}
