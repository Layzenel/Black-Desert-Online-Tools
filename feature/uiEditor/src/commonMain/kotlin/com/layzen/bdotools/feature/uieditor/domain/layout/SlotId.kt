package com.layzen.bdotools.feature.uieditor.domain.layout

/** Слот с номером [number] (с единицы, как подписано в игре) в группе [group]. */
data class SlotId(val group: SlotGroup, val number: Int) {
    /** `Index` элемента `UIData` этого слота в `gamevariable.xml`. */
    val uiIndex: Int get() = group.uiIndices[number - 1]

    /** Какой это элемент, если он из группы [SlotGroup.Interface], иначе `null`. */
    val interfaceElement: InterfaceElement?
        get() = if (group == SlotGroup.Interface) InterfaceElement.entries[number - 1] else null

    /**
     * Правит ли редактор видимость элемента. Нет — у тех, чью видимость меняет сама игра
     * ([InterfaceElement.visibilityByGame]): они всегда показаны видимыми, а `IsShow` в файле не трогается.
     */
    val hasVisibilityToggle: Boolean get() = interfaceElement?.visibilityByGame != true

    /** Ширина рамки в UI-px. */
    val width: Int get() = interfaceElement?.width ?: group.width

    /** Высота рамки в UI-px. */
    val height: Int get() = interfaceElement?.height ?: group.height

    /**
     * Есть ли элемент на экране игры при «Настройке быстрых слотов» [customQuickSlots] (см. [SlotLayout.customQuickSlots]):
     * 20 слотов хотбара и «Быстрый слот» друг друга заменяют. `null` — как включённая.
     */
    fun isActiveWith(customQuickSlots: Boolean?): Boolean = when {
        group == SlotGroup.Hotbar -> customQuickSlots != false
        interfaceElement == InterfaceElement.QuickSlot -> customQuickSlots == false
        else -> true
    }
}
