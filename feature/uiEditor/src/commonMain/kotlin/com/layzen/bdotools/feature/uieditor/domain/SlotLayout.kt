package com.layzen.bdotools.feature.uieditor.domain

/**
 * Раскладка: позиция каждого элемента, какие из них скрыты и ширина заглушек надписей ([labels], в символах,
 * см. [SlotLabel]). Может содержать не все элементы — готовые пресеты, например, задают только слоты перезарядки
 * и не трогают хотбар. Надписи живут только в пресетах и файлах раскладки: в `gamevariable.xml` их нет.
 *
 * [customQuickSlots] — «Настройка быстрых слотов» игры: `true` — хотбар из 20 отдельных слотов, `false` — одна
 * панель «Быстрый слот» ([InterfaceElement.QuickSlot]); `null` — раскладка её не задаёт и не меняет.
 */
data class SlotLayout(
    val positions: Map<SlotId, SlotPosition> = emptyMap(),
    val hidden: Set<SlotId> = emptySet(),
    val labels: Map<SlotId, Int> = emptyMap(),
    val customQuickSlots: Boolean? = null,
) {

    operator fun get(id: SlotId): SlotPosition? = positions[id]

    fun isVisible(id: SlotId): Boolean = id !in hidden

    /**
     * Позиции, видимость и надписи из [other] поверх текущих. Видимость и надписи меняются только у элементов,
     * которые есть в [other].
     */
    fun overlaidWith(other: SlotLayout) = SlotLayout(
        positions = positions + other.positions,
        hidden = (hidden - other.positions.keys) + other.hidden,
        labels = (labels - other.positions.keys) + other.labels,
        customQuickSlots = other.customQuickSlots ?: customQuickSlots,
    )

    fun withPosition(id: SlotId, position: SlotPosition) = copy(positions = positions + (id to position))

    fun withVisibility(id: SlotId, visible: Boolean) = copy(hidden = if (visible) hidden - id else hidden + id)

    /** Переключает «Настройку быстрых слотов» вместе с видимостью хотбара и «Быстрого слота», см. [hiddenWithCustomQuickSlots]. */
    fun withCustomQuickSlots(enabled: Boolean) =
        copy(customQuickSlots = enabled, hidden = hiddenWithCustomQuickSlots(hidden, enabled))

    /** Заглушка надписи шириной [chars] символов; `null` убирает надпись. */
    fun withLabel(id: SlotId, chars: Int?) = copy(labels = if (chars == null) labels - id else labels + (id to chars))

    /** Рамка, охватывающая элементы [ids]; `null`, если ни одного из них нет в раскладке. */
    fun boundsOf(ids: Set<SlotId>): SlotBounds? =
        SlotBounds.union(ids.mapNotNull { id -> this[id]?.let { SlotBounds.of(id, it) } })

    fun moved(ids: Set<SlotId>, dx: Int, dy: Int) = transform(ids) { it.movedBy(dx, dy) }

    private fun transform(ids: Set<SlotId>, change: (SlotPosition) -> SlotPosition) =
        copy(positions = positions.mapValues { (id, position) -> if (id in ids) change(position) else position })
}
