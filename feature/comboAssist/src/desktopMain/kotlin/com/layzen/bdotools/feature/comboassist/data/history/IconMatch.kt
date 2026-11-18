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

/** Порог совпадения: до [STRICT_SCORE] с отрывом [STRICT_GAP] или до [LOOSE_SCORE] с отрывом [LOOSE_GAP]. */
internal fun accepts(score: Double, second: Double): Boolean =
    (score <= STRICT_SCORE && second - score >= STRICT_GAP) || (score <= LOOSE_SCORE && second - score >= LOOSE_GAP)
