package com.layzen.bdotools.feature.uieditor.domain.game

import com.layzen.bdotools.feature.uieditor.domain.layout.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.screen.movedTo
import com.layzen.bdotools.feature.uieditor.domain.screen.position

/**
 * Всё, что фиче нужно из `gamevariable.xml`: экран игры, записи `UIData` элементов
 * (ключ — `Index`) и «Настройка быстрых слотов» ([SlotLayout.customQuickSlots]; `null` — её нет в файле).
 * Элемента без записи в файле нет в [uiData]. [chats] — видимые в игре окна чата по `WindowIndex`.
 * [splitHp] — «Разделение HP» ([SlotLayout.splitHp]; `null` — её нет в файле).
 */
data class GameVariable(
    val screen: ScreenSpace,
    val uiData: Map<Int, UiData>,
    val customQuickSlots: Boolean? = null,
    val chats: Map<Int, ChatWindow> = emptyMap(),
    val splitHp: Boolean? = null,
) {

    /** Элементы, у которых есть запись в файле. */
    val slots: List<SlotId> get() = SlotGroup.entries.flatMap { it.slots }.filter { it.uiIndex in uiData }

    /** Раскладка, которая сейчас сохранена в файле. */
    val layout: SlotLayout
        get() = SlotLayout(
            positions = slots.associateWith { uiData.getValue(it.uiIndex).position(screen) },
            hidden = slots.filterTo(mutableSetOf()) { it.hasVisibilityToggle && !uiData.getValue(it.uiIndex).isShow },
            customQuickSlots = customQuickSlots,
            chats = chats,
            splitHp = splitHp,
        )

    /** Задаёт ли [target] «Настройку быстрых слотов» не такую, как в файле. */
    fun quickSlotsChanged(target: SlotLayout): Boolean =
        target.customQuickSlots != null && target.customQuickSlots != customQuickSlots

    /** Задаёт ли [target] «Разделение HP» не такое, как в файле. */
    fun splitHpChanged(target: SlotLayout): Boolean = target.splitHp != null && target.splitHp != splitHp

    /** Окна чата из [target], которые отличаются от файла; окон, которых нет в файле, здесь нет. */
    fun changedChats(target: SlotLayout): Map<Int, ChatWindow> =
        target.chats.filter { (window, chat) -> chats[window]?.let { it != chat } == true }

    /** Записи, которые нужно заменить в файле, чтобы игра поставила элементы по [target]. */
    fun changedEntries(target: SlotLayout): List<UiData> = slots.mapNotNull { id ->
        val entry = uiData.getValue(id.uiIndex)
        val position = target[id] ?: return@mapNotNull null
        val moved = if (entry.position(screen) == position) entry else entry.movedTo(position, screen)
        // Видимость, которую меняет сама игра, оставляем как в файле
        val changed = if (id.hasVisibilityToggle) moved.copy(isShow = target.isVisible(id)) else moved
        changed.takeIf { it != entry }
    }
}
