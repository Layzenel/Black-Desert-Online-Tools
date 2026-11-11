package com.layzen.bdotools.feature.uieditor.domain.layout

/**
 * Одиночный элемент интерфейса вне рядов слотов: полоски состояния и история событий у персонажа.
 *
 * [uiIndex] — `Index` записи `UIData`, [width] и [height] — рамка в режиме правки интерфейса игры в UI-px:
 * замерены по скриншоту 2560×1440 при масштабе 1.0. Окна по краям экрана (задания, мини-карта, чаты, рейд,
 * транспорт) игрок ставит в самой игре, поэтому их здесь нет. Историю использования навыков тоже не правим.
 */
enum class InterfaceElement(val uiIndex: Int, val width: Int, val height: Int, val visibilityByGame: Boolean = false) {
    Health(uiIndex = 13, width = 385, height = 40),

    /** Выносливость: игра сама показывает её при расходе и прячет, поэтому видимость не правим ([visibilityByGame]). */
    Stamina(uiIndex = 122, width = 245, height = 16, visibilityByGame = true),
    Pvp(uiIndex = 84, width = 50, height = 49),
    BlackSpiritRage(uiIndex = 85, width = 47, height = 47),
    Buffs(uiIndex = 119, width = 410, height = 160),
    RareItemHistory(uiIndex = 161, width = 416, height = 84),

    /**
     * «Быстрый слот» — одна панель хотбара, которую игра показывает при выключенной «Настройке быстрых слотов»
     * (см. [SlotLayout.customQuickSlots]). Размер — как у самой панели в игре, а не у рамки в режиме правки.
     */
    QuickSlot(uiIndex = 12, width = 545, height = 52),
    ;

    /** Элемент как [SlotId] группы [SlotGroup.Interface]. */
    val slotId: SlotId get() = SlotId(SlotGroup.Interface, ordinal + 1)
}
