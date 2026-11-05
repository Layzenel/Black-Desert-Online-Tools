package com.layzen.bdotools.feature.slots.domain

import kotlin.math.roundToInt

/** Выровненные по [edge] элементы [ids]: края или середины встают на границу рамки всей группы. */
fun SlotLayout.aligned(ids: Set<SlotId>, edge: AlignEdge): SlotLayout {
    val box = boundsOf(ids) ?: return this
    return movedEach(ids) { _, bounds ->
        when (edge) {
            AlignEdge.Left -> (box.left - bounds.left) to 0.0
            AlignEdge.CenterX -> (box.centerX - bounds.centerX) to 0.0
            AlignEdge.Right -> (box.right - bounds.right) to 0.0
            AlignEdge.Top -> 0.0 to (box.top - bounds.top)
            AlignEdge.CenterY -> 0.0 to (box.centerY - bounds.centerY)
            AlignEdge.Bottom -> 0.0 to (box.bottom - bounds.bottom)
        }
    }
}

/**
 * Элементы [ids] с равными промежутками между рамками по горизонтали ([horizontal]) или по вертикали.
 * Крайние остаются на месте. Меньше трёх элементов — раскладка не меняется.
 */
fun SlotLayout.distributed(ids: Set<SlotId>, horizontal: Boolean): SlotLayout {
    val items = ids.mapNotNull { id -> this[id]?.let { id to SlotBounds.of(id, it) } }
    if (items.size < 3) return this
    fun start(bounds: SlotBounds) = if (horizontal) bounds.left else bounds.top
    fun size(bounds: SlotBounds) = if (horizontal) bounds.width else bounds.height
    val sorted = items.sortedBy { start(it.second) }
    val first = start(sorted.first().second)
    val last = sorted.last().second.let { start(it) + size(it) }
    val gap = (last - first - sorted.sumOf { size(it.second) }) / (sorted.size - 1)
    var cursor = first
    val shifts = sorted.associate { (id, bounds) ->
        val shift = cursor - start(bounds)
        cursor += size(bounds) + gap
        id to shift
    }
    return movedEach(ids) { id, _ -> shifts.getValue(id).let { if (horizontal) it to 0.0 else 0.0 to it } }
}

/** Группа [ids] целиком сдвинута так, что середина её рамки стоит в [target]. */
fun SlotLayout.centeredAt(ids: Set<SlotId>, target: SlotPosition): SlotLayout {
    val box = boundsOf(ids) ?: return this
    return moved(ids, (target.x - box.centerX).roundToInt(), (target.y - box.centerY).roundToInt())
}

/** Элементы [ids] отражены слева направо относительно вертикали, проходящей через [axisX]. */
fun SlotLayout.mirrored(ids: Set<SlotId>, axisX: Int): SlotLayout =
    copy(positions = positions.mapValues { (id, position) -> if (id in ids) SlotPosition(2 * axisX - position.x, position.y) else position })

/** Каждый элемент из [ids], что есть в раскладке, сдвинут на (dx, dy) UI-px, которые [shiftOf] считает по его id и рамке. */
private fun SlotLayout.movedEach(ids: Set<SlotId>, shiftOf: (SlotId, SlotBounds) -> Pair<Double, Double>): SlotLayout =
    copy(
        positions = positions.mapValues { (id, position) ->
            if (id !in ids) return@mapValues position
            val (dx, dy) = shiftOf(id, SlotBounds.of(id, position))
            position.movedBy(dx.roundToInt(), dy.roundToInt())
        },
    )
