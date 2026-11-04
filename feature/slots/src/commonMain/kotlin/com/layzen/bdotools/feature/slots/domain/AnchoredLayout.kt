package com.layzen.bdotools.feature.slots.domain

import kotlin.math.roundToInt

/** Самый мелкий масштаб интерфейса, который предлагаем при импорте: меньше в игре не бывает. */
private const val MIN_UI_SCALE = 0.5

/** Шаг подбора масштаба интерфейса, как у ползунка в настройках игры. */
private const val UI_SCALE_STEP = 0.05

/**
 * Раскладка, не привязанная к монитору: каждый элемент — смещение от своего якоря.
 * Так хранятся пресеты и файлы экспорта; на конкретный экран её ставит [placeOn].
 */
data class AnchoredLayout(
    val positions: Map<SlotId, AnchoredPosition> = emptyMap(),
    val hidden: Set<SlotId> = emptySet(),
) {

    fun placeOn(screen: ScreenSpace) = SlotLayout(positions.mapValues { it.value.placeOn(screen) }, hidden)

    /** Только элементы [ids]: так импортируется и экспортируется часть раскладки. */
    fun only(ids: Set<SlotId>) = AnchoredLayout(positions.filterKeys { it in ids }, hidden intersect ids)

    /** Сколько элементов не помещается на экран [screen]. */
    fun offScreenCount(screen: ScreenSpace): Int =
        positions.count { (id, position) -> !screen.fits(id, position.placeOn(screen)) }

    /**
     * Наибольший масштаб интерфейса меньше текущего, при котором на этом мониторе помещается всё.
     * `null` — если уже помещается или не помещается даже при самом мелком масштабе.
     */
    fun suggestedUiScale(screen: ScreenSpace): Double? {
        if (offScreenCount(screen) == 0) return null
        var steps = (screen.uiScale / UI_SCALE_STEP).roundToInt() - 1
        while (steps * UI_SCALE_STEP >= MIN_UI_SCALE - 1e-9) {
            val scale = steps * UI_SCALE_STEP
            if (offScreenCount(screen.copy(uiScale = scale)) == 0) return (scale * 100).roundToInt() / 100.0
            steps--
        }
        return null
    }

    companion object {
        /** Запоминает раскладку экрана [screen] от ближайших якорей. */
        fun of(layout: SlotLayout, screen: ScreenSpace) = AnchoredLayout(
            positions = layout.positions.mapValues { AnchoredPosition.of(it.value, screen) },
            hidden = layout.hidden,
        )
    }
}
