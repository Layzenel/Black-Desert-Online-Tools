package com.layzen.bdotools.feature.slots.domain

/**
 * Экран в единицах интерфейса игры (UI-px): разрешение, делённое на масштаб интерфейса.
 * Например, 2560×1440 при масштабе 0.8 — это 3200×1800 UI-px.
 */
data class ScreenSpace(val width: Double, val height: Double) {

    /** Левый верхний угол рамки слота с центром в [position]. */
    fun frameLeft(group: SlotGroup, position: SlotPosition): Double = width / 2 + position.x - group.width / 2.0

    fun frameTop(group: SlotGroup, position: SlotPosition): Double = height / 2 + position.y - group.height / 2.0

    /** Помещается ли рамка слота на экран целиком. */
    fun fits(id: SlotId, position: SlotPosition): Boolean {
        val left = frameLeft(id.group, position)
        val top = frameTop(id.group, position)
        return left >= 0 && top >= 0 && left + id.group.width <= width && top + id.group.height <= height
    }

    companion object {
        fun of(resolutionWidth: Int, resolutionHeight: Int, uiScale: Double) =
            ScreenSpace(resolutionWidth / uiScale, resolutionHeight / uiScale)
    }
}
