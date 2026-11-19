package com.layzen.bdotools.feature.comboassist.domain.graph

import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt

/**
 * Шаг комбо в графе: заполненная ячейка.
 *
 * @property id номер узла в графе, по порядку колонок и веток
 * @property skillNo умение
 * @property at адрес ячейки
 * @property isFork ячейка стоит в развилке
 * @property chain связка с предыдущим шагом
 * @property optional опциональный шаг
 * @property label подпись ячейки («4.1»)
 */
class ComboNode(
    val id: Int,
    val skillNo: Int,
    val at: CellAt,
    val isFork: Boolean,
    val chain: Boolean,
    val optional: Boolean,
    val label: String,
) {
    /** Шаги, которые можно сделать после этого. */
    val next: MutableList<ComboNode> = mutableListOf()

    /** Шаги, после которых можно сделать этот. */
    val previous: MutableList<ComboNode> = mutableListOf()

    /** Следующий шаг той же ветки развилки, если он есть. */
    fun nextInLane(): ComboNode? = if (!isFork) null else next.firstOrNull { it.at.column == at.column && it.at.lane == at.lane && it.at.index == at.index + 1 }

    /** Продолжение связки после этого шага. */
    fun chainNext(): ComboNode? = next.firstOrNull { it.chain }

    /** Переход к [other] начинает новый проход комбо: колонка назад или другая ветка той же развилки. */
    fun wrapsTo(other: ComboNode): Boolean =
        other.at.column < at.column || (other.at.column == at.column && (other.at.lane != at.lane || other.at.index <= at.index))

    override fun toString(): String = "ComboNode($label, $skillNo)"
}
