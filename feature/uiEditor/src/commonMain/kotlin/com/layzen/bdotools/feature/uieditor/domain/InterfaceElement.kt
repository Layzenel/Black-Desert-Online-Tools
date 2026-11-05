package com.layzen.bdotools.feature.uieditor.domain

/**
 * Одиночный элемент интерфейса вне рядов слотов: полоски состояния и история событий у персонажа.
 *
 * [uiIndex] — `Index` записи `UIData`, [width] и [height] — рамка в режиме правки интерфейса игры в UI-px:
 * замерены по скриншоту 2560×1440 при масштабе 1.0. Окна по краям экрана (задания, мини-карта, чаты, рейд,
 * транспорт) игрок ставит в самой игре, поэтому их здесь нет. Историю использования навыков тоже не правим.
 */
enum class InterfaceElement(val uiIndex: Int, val width: Int, val height: Int) {
    Health(uiIndex = 13, width = 385, height = 40),
    Stamina(uiIndex = 122, width = 245, height = 16),
    Pvp(uiIndex = 84, width = 50, height = 49),
    BlackSpiritRage(uiIndex = 85, width = 47, height = 47),
    Buffs(uiIndex = 119, width = 410, height = 160),
    RareItemHistory(uiIndex = 161, width = 416, height = 84),
}
