package com.layzen.bdotools.feature.slots.domain

/**
 * Раскладка: позиция каждого элемента и какие из них скрыты. Может содержать не все элементы —
 * готовые пресеты, например, задают только слоты перезарядки и не трогают хотбар.
 */
data class SlotLayout(
    val positions: Map<SlotId, SlotPosition> = emptyMap(),
    val hidden: Set<SlotId> = emptySet(),
) {

    operator fun get(id: SlotId): SlotPosition? = positions[id]

    fun isVisible(id: SlotId): Boolean = id !in hidden

    /**
     * Позиции и видимость из [other] поверх текущих. Видимость меняется только у элементов,
     * которые есть в [other].
     */
    fun overlaidWith(other: SlotLayout) = SlotLayout(
        positions = positions + other.positions,
        hidden = (hidden - other.positions.keys) + other.hidden,
    )

    fun withPosition(id: SlotId, position: SlotPosition) = copy(positions = positions + (id to position))

    fun withVisibility(id: SlotId, visible: Boolean) = copy(hidden = if (visible) hidden - id else hidden + id)

    fun moved(ids: Set<SlotId>, dx: Int, dy: Int) = transform(ids) { it.movedBy(dx, dy) }

    fun mirrored(ids: Set<SlotId>) = transform(ids, SlotPosition::mirrored)

    private fun transform(ids: Set<SlotId>, change: (SlotPosition) -> SlotPosition) =
        copy(positions = positions.mapValues { (id, position) -> if (id in ids) change(position) else position })
}
