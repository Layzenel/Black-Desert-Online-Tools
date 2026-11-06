package com.layzen.bdotools.feature.uieditor.domain

/**
 * Всё, что фиче нужно из `gamevariable.xml`: экран игры, записи `UIData` элементов
 * (ключ — `Index`) и «Настройка быстрых слотов» ([SlotLayout.customQuickSlots]; `null` — её нет в файле).
 * Элемента без записи в файле нет в [uiData].
 */
data class GameVariable(val screen: ScreenSpace, val uiData: Map<Int, UiData>, val customQuickSlots: Boolean? = null) {

    /** Элементы, у которых есть запись в файле. */
    val slots: List<SlotId> get() = SlotGroup.entries.flatMap { it.slots }.filter { it.uiIndex in uiData }

    /** Раскладка, которая сейчас сохранена в файле. */
    val layout: SlotLayout
        get() = SlotLayout(
            positions = slots.associateWith { uiData.getValue(it.uiIndex).position(screen) },
            hidden = slots.filterTo(mutableSetOf()) { !uiData.getValue(it.uiIndex).isShow },
            customQuickSlots = customQuickSlots,
        )

    /** Задаёт ли [target] «Настройку быстрых слотов» не такую, как в файле. */
    fun quickSlotsChanged(target: SlotLayout): Boolean =
        target.customQuickSlots != null && target.customQuickSlots != customQuickSlots

    /** Записи, которые нужно заменить в файле, чтобы игра поставила элементы по [target]. */
    fun changedEntries(target: SlotLayout): List<UiData> = slots.mapNotNull { id ->
        val entry = uiData.getValue(id.uiIndex)
        val position = target[id] ?: return@mapNotNull null
        val moved = if (entry.position(screen) == position) entry else entry.movedTo(position, screen)
        val changed = moved.copy(isShow = target.isVisible(id))
        changed.takeIf { it != entry }
    }
}
