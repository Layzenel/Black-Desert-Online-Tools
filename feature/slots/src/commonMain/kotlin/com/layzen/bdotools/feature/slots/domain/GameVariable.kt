package com.layzen.bdotools.feature.slots.domain

/**
 * Всё, что фиче нужно из `gamevariable.xml`: экран игры и записи `UIData` слотов
 * (ключ — `Index`). Слота без записи в файле нет в [uiData].
 */
data class GameVariable(val screen: ScreenSpace, val uiData: Map<Int, UiData>) {

    /** Раскладка, которая сейчас сохранена в файле. */
    val layout: SlotLayout
        get() = SlotLayout(
            allSlots().mapNotNull { id -> uiData[id.uiIndex]?.let { id to it.position(id.group, screen) } }.toMap(),
        )

    /** Слоты, которые игра показывает (`IsShow="true"`): только их видно и можно двигать в редакторе. */
    val visibleSlots: List<SlotId>
        get() = allSlots().filter { uiData[it.uiIndex]?.isShow == true }

    /** Записи, которые нужно заменить в файле, чтобы игра поставила слоты по [target]. */
    fun changedEntries(target: SlotLayout): List<UiData> =
        target.positions.mapNotNull { (id, position) ->
            val entry = uiData[id.uiIndex] ?: return@mapNotNull null
            if (entry.position(id.group, screen) == position) null else entry.movedTo(position, id.group, screen)
        }

    private fun allSlots() = SlotGroup.entries.flatMap { it.slots }
}
