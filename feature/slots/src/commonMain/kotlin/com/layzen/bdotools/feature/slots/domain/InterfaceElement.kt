package com.layzen.bdotools.feature.slots.domain

/**
 * Одиночный элемент интерфейса вне рядов слотов: полоски, окна, история событий.
 *
 * [uiIndex] — `Index` записи `UIData`, [width] и [height] — рамка в режиме правки интерфейса игры в UI-px:
 * замерены по скриншоту 2560×1440 при масштабе 1.0. У окон заданий размер берётся из их `SizeX`/`SizeY`.
 * Элементы с пометкой «[не изменить]» в игре (чаты, мини-карта, меню) сюда не входят.
 */
enum class InterfaceElement(val uiIndex: Int, val width: Int, val height: Int) {
    Health(uiIndex = 13, width = 385, height = 40),
    Stamina(uiIndex = 122, width = 245, height = 16),
    Pvp(uiIndex = 84, width = 50, height = 49),
    BlackSpiritRage(uiIndex = 85, width = 47, height = 47),
    Reload(uiIndex = 111, width = 41, height = 41),
    Transport(uiIndex = 139, width = 50, height = 50),
    Raid(uiIndex = 15, width = 320, height = 24),

    /** Вторая рамка «Рейд»: в игре лежит почти на первой, чем отличается — не выяснено. */
    RaidSecond(uiIndex = 127, width = 300, height = 24),
    Buffs(uiIndex = 119, width = 410, height = 160),
    SkillHistory(uiIndex = 158, width = 200, height = 80),
    Kills(uiIndex = 143, width = 328, height = 100),
    ControlHints(uiIndex = 88, width = 300, height = 250),
    RareItemHistory(uiIndex = 161, width = 416, height = 84),
    TrophyHistory(uiIndex = 159, width = 350, height = 300),
    VirtualKeyboard(uiIndex = 112, width = 660, height = 250),

    // shortcut: размер окон фиксирован по умолчанию игры, а игрок может их растянуть (SizeX/SizeY) — читать из файла, если понадобится
    Quests(uiIndex = 21, width = 305, height = 644),
    QuestsBasic(uiIndex = 113, width = 305, height = 80),
}
