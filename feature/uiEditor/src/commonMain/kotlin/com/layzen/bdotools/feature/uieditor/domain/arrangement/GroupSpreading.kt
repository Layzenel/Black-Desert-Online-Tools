package com.layzen.bdotools.feature.uieditor.domain.arrangement

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotBounds
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Рамки ближе этого, UI-px, — одна группа.
 * shortcut: группа — по касанию рамок; если игроки ставят колонки слотов с промежутком, понадобится ручная разметка групп.
 */
private const val GROUP_GAP = 4.0

/** Слот [slot] или отдельное окно чата [chat] с рамкой [bounds]. */
private class GroupItem(val slot: SlotId?, val chat: Int?, val bounds: SlotBounds)

/** Группа: элементы, касающиеся друг друга, и общая рамка. */
private class ElementGroup(val items: List<GroupItem>) {
    /** Общая рамка всех элементов группы. */
    val bounds: SlotBounds = requireNotNull(SlotBounds.union(items.map { it.bounds }))
}

/**
 * Отдаление групп среди элементов [ids] и окон чата [windows] по горизонтали ([horizontal]) или по вертикали:
 * от центра между группами (среднего их центров) до самой дальней группы. Группа — элементы, чьи рамки касаются
 * друг друга (колонка слотов, ряд хотбара, окно чата). `null` — меньше двух групп или они стоят на одной линии оси.
 */
fun SlotLayout.groupSpread(ids: Set<SlotId>, windows: Set<Int>, horizontal: Boolean): SpreadAxis? {
    val groups = groups(ids, windows).takeIf { it.size >= 2 } ?: return null
    val centers = groups.map { it.bounds.center(horizontal) }
    val pivot = centers.average()
    val current = centers.maxOf { abs(it - pivot) }
    if (current == 0.0) return null
    return SpreadAxis(current.roundToInt(), ceil(current * minFactor(groups, horizontal)).toInt())
}

/**
 * Раскладка, где группы элементов [ids] и окон [windows] разнесены от центра между ними на [value] UI-px по оси:
 * расстояния до центра меняются в одной пропорции, поэтому симметричная раскладка остаётся симметричной.
 * Группы целиком сдвигаются, внутри не меняются; ближе [SpreadAxis.min] не сходятся.
 */
fun SlotLayout.withGroupSpread(ids: Set<SlotId>, windows: Set<Int>, horizontal: Boolean, value: Int): SlotLayout {
    val spread = groupSpread(ids, windows, horizontal) ?: return this
    val groups = groups(ids, windows)
    val centers = groups.map { it.bounds.center(horizontal) }
    val pivot = centers.average()
    val factor = value.coerceAtLeast(spread.min).toDouble() / centers.maxOf { abs(it - pivot) }
    return groups.zip(centers).fold(this) { layout, (group, center) ->
        val shift = ((center - pivot) * (factor - 1)).roundToInt()
        layout.moved(
            group.items.mapNotNullTo(mutableSetOf()) { it.slot },
            group.items.mapNotNullTo(mutableSetOf()) { it.chat },
            if (horizontal) shift else 0,
            if (horizontal) 0 else shift,
        )
    }
}

/** Элементы, разбитые на группы касающихся: каждый новый сливает все группы, которых касается. */
private fun SlotLayout.groups(ids: Set<SlotId>, windows: Set<Int>): List<ElementGroup> {
    val items = ids.mapNotNull { id -> this[id]?.let { GroupItem(id, null, SlotBounds.of(id, it)) } } +
        separateChats(windows).map { (window, chat) -> GroupItem(null, window, chat.bounds) }
    val groups = mutableListOf<List<GroupItem>>()
    items.forEach { item ->
        val near = groups.filter { group -> group.any { it.bounds.isNear(item.bounds) } }
        groups.removeAll { it in near }
        groups += near.flatten() + item
    }
    return groups.map(::ElementGroup)
}

/**
 * Во сколько раз можно сжать расстояния до центра, чтобы группы, разнесённые по оси, не сошлись ближе [GROUP_GAP]
 * и не слились. Группы, перекрытые по оси (одна под другой при горизонтальной), не мешают.
 */
private fun minFactor(groups: List<ElementGroup>, horizontal: Boolean): Double {
    var factor = 0.0
    groups.forEachIndexed { i, a ->
        groups.drop(i + 1).forEach { b ->
            val distance = abs(a.bounds.center(horizontal) - b.bounds.center(horizontal))
            val halves = (a.bounds.size(horizontal) + b.bounds.size(horizontal)) / 2
            if (distance > halves) factor = maxOf(factor, (halves + GROUP_GAP + 1) / distance)
        }
    }
    return factor
}

private fun SlotBounds.center(horizontal: Boolean) = if (horizontal) centerX else centerY

private fun SlotBounds.size(horizontal: Boolean) = if (horizontal) width else height

private fun SlotBounds.isNear(other: SlotBounds) =
    left <= other.right + GROUP_GAP && other.left <= right + GROUP_GAP &&
        top <= other.bottom + GROUP_GAP && other.top <= bottom + GROUP_GAP
