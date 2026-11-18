package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Адрес ячейки комбо.
 *
 * @property column индекс колонки
 * @property lane индекс ветки; у обычной колонки — 0
 * @property index индекс ячейки в ветке
 */
data class CellAt(val column: Int, val lane: Int = 0, val index: Int = 0)
