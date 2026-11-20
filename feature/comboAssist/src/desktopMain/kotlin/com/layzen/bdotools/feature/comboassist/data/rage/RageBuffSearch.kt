package com.layzen.bdotools.feature.comboassist.data.rage

import kotlin.math.abs

/** Насколько, в пикселях, иконки тройки могут сбиться с ряда и шага. */
private const val TOLERANCE = 3

/**
 * Что нашёл [RageBuffMatcher] на одном снимке.
 *
 * @property places места каждого из трёх эталонов, по порядку бафов
 * @property step шаг иконок в ряду, px
 */
internal data class RageBuffSearch(val places: List<List<BuffPlace>>, val step: Int) {

    /**
     * Три иконки подряд в одном ряду, по порядку 1-2-3, с шагом [step], или `null`: так стоят бафы гнева (игра ставит
     * их вместе). Те же иконки бывают и у других бафов, поэтому гнев ещё проверяется по таймеру ([RageTimerReader]).
     */
    val triple: List<BuffPlace>?
        get() {
            if (places.size != 3) return null
            for (first in places[0]) for (second in places[1]) {
                if (!next(first, second)) continue
                places[2].firstOrNull { next(second, it) }?.let { return listOf(first, second, it) }
            }
            return null
        }

    /** Для журнала: сколько мест у каждого эталона и лучший счёт («-», если мест нет). */
    override fun toString(): String = places.joinToString(" ") { list ->
        "${list.size}/" + (list.minOfOrNull { it.score }?.let { "%.1f".format(it) } ?: "-")
    }

    private fun next(left: BuffPlace, right: BuffPlace): Boolean =
        abs(right.y - left.y) <= TOLERANCE && abs(right.x - left.x - step) <= TOLERANCE
}
