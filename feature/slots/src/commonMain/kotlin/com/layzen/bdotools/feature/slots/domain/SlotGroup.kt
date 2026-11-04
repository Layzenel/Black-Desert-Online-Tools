package com.layzen.bdotools.feature.slots.domain

/**
 * Группа слотов на экране игры.
 *
 * [uiIndices] — `Index` элементов `UIData` в `gamevariable.xml` в порядке номеров слотов в игре:
 * первый элемент — «Слот 1». [width] и [height] — размер рамки слота в режиме правки интерфейса,
 * в единицах интерфейса (UI-px). Рамки соседних слотов чуть перекрываются: шаг сетки в игре — 50.
 */
enum class SlotGroup(val uiIndices: List<Int>, val width: Int, val height: Int) {
    Cooldown(uiIndices = (128..137) + (148..157), width = 60, height = 60),
    Hotbar(uiIndices = listOf(100) + (91..99) + 110 + (101..109), width = 60, height = 78),
    ;

    /** Все слоты группы по порядку номеров. */
    val slots: List<SlotId> get() = uiIndices.indices.map { SlotId(this, it + 1) }
}
