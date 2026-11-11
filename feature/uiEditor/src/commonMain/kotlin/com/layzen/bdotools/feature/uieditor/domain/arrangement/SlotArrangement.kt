package com.layzen.bdotools.feature.uieditor.domain.arrangement

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotBounds
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import kotlin.math.roundToInt

/**
 * Выровненные по [edge] элементы [ids]: края или середины встают на край или середину опорной рамки.
 * Опора — элемент [anchor], если он среди [ids] (он сам не двигается), иначе рамка всей группы.
 */
fun SlotLayout.aligned(ids: Set<SlotId>, edge: AlignEdge, anchor: SlotId? = null): SlotLayout {
    val box = anchor?.takeIf { it in ids }?.let { boundsOf(setOf(it)) } ?: boundsOf(ids) ?: return this
    return movedEach(ids) { _, bounds ->
        when (edge) {
            AlignEdge.Top -> 0.0 to (box.top - bounds.top)
            AlignEdge.Bottom -> 0.0 to (box.bottom - bounds.bottom)
            AlignEdge.Left -> (box.left - bounds.left) to 0.0
            AlignEdge.Right -> (box.right - bounds.right) to 0.0
            AlignEdge.CenterX -> (box.centerX - bounds.centerX) to 0.0
            AlignEdge.CenterY -> 0.0 to (box.centerY - bounds.centerY)
        }
    }
}

/**
 * Элементы [ids] друг за другом по горизонтали ([horizontal]) или по вертикали с промежутком [gap] UI-px между рамками.
 * Элементы, стоящие друг над другом (одна колонка) при распределении по горизонтали — или в одном ряду
 * при распределении по вертикали, — двигаются вместе как одна рамка: так сетка из нескольких рядов не рассыпается.
 * Первая колонка (ряд) — самая левая (верхняя) — остаётся на месте. Меньше двух колонок (рядов) — раскладка не меняется.
 */
fun SlotLayout.distributed(ids: Set<SlotId>, horizontal: Boolean, gap: Int): SlotLayout {
    val items = ids.mapNotNull { id -> this[id]?.let { id to SlotBounds.of(id, it) } }
    fun start(bounds: SlotBounds) = if (horizontal) bounds.left else bounds.top
    fun size(bounds: SlotBounds) = if (horizontal) bounds.width else bounds.height
    fun center(bounds: SlotBounds) = start(bounds) + size(bounds) / 2
    // Колонка — элементы, чьи середины ближе половины размера элемента к середине первого в ней
    val lines = mutableListOf<MutableList<Pair<SlotId, SlotBounds>>>()
    items.sortedBy { center(it.second) }.forEach { item ->
        val line = lines.lastOrNull()
        if (line != null && center(item.second) - center(line.first().second) < size(item.second) / 2) line += item
        else lines += mutableListOf(item)
    }
    if (lines.size < 2) return this
    val boxes = lines.map { line -> requireNotNull(SlotBounds.union(line.map { it.second })) }
    var cursor = start(boxes.first())
    val shifts = mutableMapOf<SlotId, Double>()
    lines.zip(boxes).forEach { (line, box) ->
        val shift = cursor - start(box)
        line.forEach { (id, _) -> shifts[id] = shift }
        cursor += size(box) + gap
    }
    return movedEach(ids) { id, _ -> shifts.getValue(id).let { if (horizontal) it to 0.0 else 0.0 to it } }
}

/** Каждый элемент из [ids], что есть в раскладке, сдвинут на (dx, dy) UI-px, которые [shiftOf] считает по его id и рамке. */
private fun SlotLayout.movedEach(ids: Set<SlotId>, shiftOf: (SlotId, SlotBounds) -> Pair<Double, Double>): SlotLayout =
    copy(
        positions = positions.mapValues { (id, position) ->
            if (id !in ids) return@mapValues position
            val (dx, dy) = shiftOf(id, SlotBounds.of(id, position))
            position.movedBy(dx.roundToInt(), dy.roundToInt())
        },
    )
