package com.layzen.bdotools.feature.slots.domain

/**
 * Раскладка слотов: позиция каждого слота. Может содержать не все слоты —
 * готовые пресеты, например, задают только слоты перезарядки и не трогают хотбар.
 */
data class SlotLayout(val positions: Map<SlotId, SlotPosition> = emptyMap()) {

    operator fun get(id: SlotId): SlotPosition? = positions[id]

    /** Позиции из [other] поверх текущих. */
    fun overlaidWith(other: SlotLayout) = SlotLayout(positions + other.positions)

    fun withPosition(id: SlotId, position: SlotPosition) = SlotLayout(positions + (id to position))

    fun moved(ids: Set<SlotId>, dx: Int, dy: Int) = transform(ids) { it.movedBy(dx, dy) }

    fun mirrored(ids: Set<SlotId>) = transform(ids, SlotPosition::mirrored)

    /** Только слоты группы [group]. */
    fun of(group: SlotGroup) = SlotLayout(positions.filterKeys { it.group == group })

    private fun transform(ids: Set<SlotId>, change: (SlotPosition) -> SlotPosition) =
        SlotLayout(positions.mapValues { (id, position) -> if (id in ids) change(position) else position })
}
