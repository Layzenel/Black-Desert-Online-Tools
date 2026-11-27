package com.layzen.bdotools.feature.screenshoteditor.domain.adjust

/**
 * Ретушь цвета всего скриншота. Каждое значение — от −100 до 100, 0 — без изменений; [vignette] — от 0 до 100.
 *
 * @property exposure экспозиция: ±1 ступень (вдвое светлее или темнее)
 * @property temperature теплее (+, к жёлтому) или холоднее (−, к синему)
 * @property tint оттенок: + к пурпурному, − к зелёному
 * @property vignette затемнение к краям
 */
data class Adjustments(
    val brightness: Int = 0,
    val contrast: Int = 0,
    val saturation: Int = 0,
    val exposure: Int = 0,
    val temperature: Int = 0,
    val tint: Int = 0,
    val vignette: Int = 0,
) {
    /** Ничего не меняет: такую ретушь не рисуем. */
    val isNeutral: Boolean get() = this == Adjustments()

    /** Ретушь без виньетки меняет цвета: нужна матрица цвета. */
    val changesColor: Boolean get() = copy(vignette = 0) != Adjustments()
}
