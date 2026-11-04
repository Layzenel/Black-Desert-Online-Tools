package com.layzen.bdotools.feature.slots.domain

import kotlin.math.roundToInt

/** Где по высоте экрана игра рисует прицел: замерено по скриншоту, прицел выше центра. */
private const val CROSSHAIR_HEIGHT_SHARE = 0.4271

/**
 * Экран игры: разрешение монитора и масштаб интерфейса. Редактор считает в единицах интерфейса
 * (UI-px) — разрешение, делённое на масштаб: 2560×1440 при масштабе 0.8 — это 3200×1800 UI-px.
 */
data class ScreenSpace(val resolutionWidth: Int, val resolutionHeight: Int, val uiScale: Double) {

    val width: Double get() = resolutionWidth / uiScale
    val height: Double get() = resolutionHeight / uiScale

    /** Прицел относительно центра экрана: по горизонтали в центре, по вертикали выше. */
    val crosshair: SlotPosition get() = SlotPosition(0, ((CROSSHAIR_HEIGHT_SHARE - 0.5) * height).roundToInt())

    /** Левый край иконки с центром в [position]. */
    fun iconLeft(group: SlotGroup, position: SlotPosition): Double = width / 2 + position.x - group.width / 2.0

    fun iconTop(group: SlotGroup, position: SlotPosition): Double = height / 2 + position.y - group.height / 2.0

    /** Помещается ли иконка на экран целиком. */
    fun fits(id: SlotId, position: SlotPosition): Boolean {
        val left = iconLeft(id.group, position)
        val top = iconTop(id.group, position)
        return left >= 0 && top >= 0 && left + id.group.width <= width && top + id.group.height <= height
    }
}
