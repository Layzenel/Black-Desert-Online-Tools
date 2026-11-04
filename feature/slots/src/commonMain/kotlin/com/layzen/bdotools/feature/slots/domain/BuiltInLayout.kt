package com.layzen.bdotools.feature.slots.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Шаг между соседними слотами в игре, UI-px. */
private const val STEP = 50

/**
 * Готовые раскладки слотов перезарядки. Хотбар они не трогают. Позиции считаются от центра экрана,
 * персонаж в игре стоит чуть ниже центра — поэтому раскладки сдвинуты вниз.
 */
enum class BuiltInLayout {
    /** Два ряда по десять под персонажем. */
    TwoRows,

    /** Две колонки 2×5 слева и справа от персонажа. */
    Sides,

    /** Дуга над персонажем. */
    Arc,

    /** Плотный блок 5×4 под персонажем. */
    Block,
    ;

    val layout: SlotLayout
        get() = SlotLayout(SlotGroup.Cooldown.slots.associateWith { position(it.number - 1) })

    private fun position(i: Int): SlotPosition = when (this) {
        TwoRows -> SlotPosition(-STEP * 9 / 2 + (i % 10) * STEP, 300 + (i / 10) * STEP)
        Sides -> {
            val side = if (i < 10) -1 else 1
            val k = i % 10
            SlotPosition(side * (375 + (k % 2) * STEP), -100 + (k / 2) * STEP)
        }
        Arc -> {
            val angle = (200 + i * 140.0 / 19) * PI / 180
            SlotPosition((cos(angle) * 400).roundToInt(), (sin(angle) * 400).roundToInt() + 250)
        }
        Block -> SlotPosition(-STEP * 2 + (i % 5) * STEP, 250 + (i / 5) * STEP)
    }
}
