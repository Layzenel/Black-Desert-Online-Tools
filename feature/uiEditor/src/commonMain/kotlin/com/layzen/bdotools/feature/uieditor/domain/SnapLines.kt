package com.layzen.bdotools.feature.uieditor.domain

/** Линии, к которым прилипает двигаемое: вертикальные [x] и горизонтальные [y], в UI-px от центра экрана. */
data class SnapLines(val x: List<Double>, val y: List<Double>)

/**
 * Линии прилипания раскладки: оси прицела [crosshair], края и середины видимых элементов из [slots] и отдельных
 * окон чата, которые видны в игре. Не участвуют то, что двигается сейчас: элементы [movingSlots] и окно [movingChat].
 */
fun SlotLayout.snapLines(slots: List<SlotId>, movingSlots: Set<SlotId>, movingChat: Int?, crosshair: SlotPosition): SnapLines {
    val slotBounds = slots
        .filter { it !in movingSlots && isVisible(it) }
        .mapNotNull { id -> this[id]?.let { SlotBounds.of(id, it) } }
    val chatBounds = chats.filter { (window, _) -> window != movingChat && isChatShown(window) }.values.map { it.bounds }
    val bounds = slotBounds + chatBounds
    return SnapLines(
        x = listOf(crosshair.x.toDouble()) + bounds.flatMap { it.xLines },
        y = listOf(crosshair.y.toDouble()) + bounds.flatMap { it.yLines },
    )
}
