package com.layzen.bdotools.feature.comboassist.data.history

/**
 * Результат сравнения иконки.
 *
 * @property skillNo лучший кандидат или `null`, если сравнивать не с чем
 * @property score его средняя разница (0–255, меньше — похоже)
 * @property second разница второго кандидата
 */
internal data class IconMatch(val skillNo: Int?, val score: Double, val second: Double) {
    /** Уверенное совпадение: близко и с отрывом от второго; чем ближе, тем меньший отрыв нужен. */
    val accepted: Boolean get() = skillNo != null && accepts(score, second)
}

private const val STRICT_SCORE = 30.0
private const val STRICT_GAP = 10.0
private const val LOOSE_SCORE = 40.0
private const val LOOSE_GAP = 20.0

// замер 2026-11-19: «Мощь: Инь и Ян IV» в истории темнее иконки справочника — 45.7 при втором 85.2;
// чужие иконки дают 36–57 без отрыва, так что большой отрыв отличает своё умение и при таком счёте
private const val WIDE_SCORE = 55.0
private const val WIDE_GAP = 30.0

/**
 * Порог совпадения: до [STRICT_SCORE] с отрывом [STRICT_GAP], до [LOOSE_SCORE] с отрывом [LOOSE_GAP] или до
 * [WIDE_SCORE] с отрывом [WIDE_GAP].
 */
internal fun accepts(score: Double, second: Double): Boolean {
    val gap = second - score
    return (score <= STRICT_SCORE && gap >= STRICT_GAP) || (score <= LOOSE_SCORE && gap >= LOOSE_GAP) || (score <= WIDE_SCORE && gap >= WIDE_GAP)
}
