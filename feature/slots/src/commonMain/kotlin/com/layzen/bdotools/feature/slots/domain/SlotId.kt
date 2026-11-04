package com.layzen.bdotools.feature.slots.domain

/** Слот с номером [number] (с единицы, как подписано в игре) в группе [group]. */
data class SlotId(val group: SlotGroup, val number: Int) {
    /** `Index` элемента `UIData` этого слота в `gamevariable.xml`. */
    val uiIndex: Int get() = group.uiIndices[number - 1]
}
