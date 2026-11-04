package com.layzen.bdotools.feature.slots.domain

/**
 * Группа элементов интерфейса, которые двигает редактор.
 *
 * [uiIndices] — `Index` элементов `UIData` в `gamevariable.xml` в порядке номеров в игре:
 * первый элемент — «Слот 1». [width] и [height] — размер рамки элемента в режиме правки интерфейса игры,
 * в UI-px: замерен по скриншоту 2560×1440 при масштабе 0.8. Рамки соседних слотов при шаге 50 стоят вплотную.
 */
enum class SlotGroup(val uiIndices: List<Int>, val width: Int, val height: Int) {
    Cooldown(uiIndices = (128..137) + (148..157), width = 50, height = 50),
    Hotbar(uiIndices = listOf(100) + (91..99) + 110 + (101..109), width = 50, height = 65),

    /** Классовый ресурс — индикатор класса, один на экран. */
    ClassResource(uiIndices = listOf(89), width = 50, height = 50),
    ;

    /** Все элементы группы по порядку номеров. */
    val slots: List<SlotId> get() = uiIndices.indices.map { SlotId(this, it + 1) }
}
