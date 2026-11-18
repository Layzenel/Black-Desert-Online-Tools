package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Колонка комбо слева направо: одно умение или развилка из нескольких веток.
 *
 * Ветка развилки может быть длиннее одной колонки: ветка из двух ячеек занимает колонки N и N + 1
 * (ячейки «N.1 → N+1.1»).
 */
sealed interface ComboColumn {

    /** Ветки колонки: у обычной — одна ветка из одной ячейки. */
    val lanes: List<List<ComboCell>>

    /** Ширина в колонках: длина самой длинной ветки. */
    val width: Int get() = lanes.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1

    /** Обычная колонка из одного умения. */
    data class Single(val cell: ComboCell) : ComboColumn {
        override val lanes: List<List<ComboCell>> get() = listOf(listOf(cell))
    }

    /**
     * Развилка: ветки друг под другом, игрок проходит одну из них. [linked] — ветка та же, что в предыдущей развилке
     * (вошёл в 1.2 — дальше 4.2): развилки связаны, хотя между ними общие шаги.
     */
    data class Fork(override val lanes: List<List<ComboCell>>, val linked: Boolean = false) : ComboColumn
}
