package com.layzen.bdotools.feature.uieditor.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Шаг между соседними слотами в игре, UI-px. */
private const val STEP = 50

/** Сколько слотов перезарядки расставляет раскладка. */
private const val SLOT_COUNT = 20

/**
 * Готовые раскладки слотов перезарядки. Хотбар они не трогают. Позиции считаются от центра экрана,
 * персонаж в игре стоит чуть ниже центра — поэтому раскладки сдвинуты вниз.
 */
enum class BuiltInLayout {
    /** Две колонки 2×5 слева и справа от персонажа. */
    Sides,

    /** Пологая дуга под персонажем, концами вверх. */
    Arc,

    /** Подкова под персонажем: дно из 8 слотов и два плеча по 6 вверх. */
    Horseshoe,

    /** Кольцо из 20 слотов вокруг прицела: взгляд не уходит от центра. */
    Ring,

    /** Крылья: две «лесенки» 2×5, расходящиеся вниз и в стороны. */
    Wings,

    /** 5×2 по краям: левый — под клавиатуру, правый — под мышь. */
    FivePlusFiveAndFivePlusFive,

    /** Четыре блока по 5 (3 сверху, 2 снизу) в ряд под персонажем — по одному на группу клавиш. */
    KeyClusters,
    ;

    val layout: SlotLayout
        get() = SlotLayout(SlotGroup.Cooldown.slots.associateWith { position(it.number - 1) })

    private fun position(i: Int): SlotPosition = when (this) {
        Sides -> {
            val side = if (i < 10) -1 else 1
            val k = i % 10
            SlotPosition(side * (375 + (k % 2) * STEP), -100 + (k / 2) * STEP)
        }
        Arc -> polar(angleDeg = 25 + i * 130.0 / (SLOT_COUNT - 1), radius = 620, centerY = -300)
        Horseshoe -> when {
            i < 8 -> SlotPosition(-STEP * 7 / 2 + i * STEP, 330)
            else -> {
                val k = i - 8
                SlotPosition((if (k % 2 == 0) -1 else 1) * 250, 330 - (k / 2 + 1) * STEP)
            }
        }
        Ring -> polar(angleDeg = -90 + i * 360.0 / SLOT_COUNT, radius = 240, centerY = 0)
        Wings -> {
            val side = if (i < 10) -1 else 1
            val k = i % 10
            val row = k / 2
            SlotPosition(side * (150 + row * 35 + (k % 2) * STEP), 150 + row * STEP)
        }
        FivePlusFiveAndFivePlusFive -> {
            val side = if (i < 10) -1 else 1
            val k = i % 10
            // Внешний край блока — 400 от центра, внутренний — 200
            SlotPosition(side * (200 + (if (side < 0) 4 - k % 5 else k % 5) * STEP), 360 + (k / 5) * STEP)
        }
        KeyClusters -> {
            val block = i / 5
            val k = i % 5
            val x = -330 + block * 220 + if (k < 3) (k - 1) * STEP else (k - 3) * STEP - STEP / 2
            SlotPosition(x, if (k < 3) 320 else 370)
        }
    }

    /** Точка на окружности радиуса [radius] с центром (0, [centerY]); угол от оси x, по часовой (y вниз). */
    private fun polar(angleDeg: Double, radius: Int, centerY: Int): SlotPosition {
        val angle = angleDeg * PI / 180
        return SlotPosition((cos(angle) * radius).roundToInt(), (sin(angle) * radius).roundToInt() + centerY)
    }
}
