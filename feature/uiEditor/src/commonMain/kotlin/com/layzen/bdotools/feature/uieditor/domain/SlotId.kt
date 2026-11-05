package com.layzen.bdotools.feature.uieditor.domain

/** Слот с номером [number] (с единицы, как подписано в игре) в группе [group]. */
data class SlotId(val group: SlotGroup, val number: Int) {
    /** `Index` элемента `UIData` этого слота в `gamevariable.xml`. */
    val uiIndex: Int get() = group.uiIndices[number - 1]

    /** Какой это элемент, если он из группы [SlotGroup.Interface], иначе `null`. */
    val interfaceElement: InterfaceElement?
        get() = if (group == SlotGroup.Interface) InterfaceElement.entries[number - 1] else null

    /** Ширина рамки в UI-px. */
    val width: Int get() = interfaceElement?.width ?: group.width

    /** Высота рамки в UI-px. */
    val height: Int get() = interfaceElement?.height ?: group.height
}
