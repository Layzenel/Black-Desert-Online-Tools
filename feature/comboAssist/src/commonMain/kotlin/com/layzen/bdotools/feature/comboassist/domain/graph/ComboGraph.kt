package com.layzen.bdotools.feature.comboassist.domain.graph

import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn

/**
 * Граф комбо: шаги и переходы между ними. Пустые ячейки пропускаются; после последней колонки переходы ведут
 * к первой колонке, а комбо начинается с первой непустой колонки.
 *
 * @property nodes шаги по порядку колонок и веток
 * @property start шаги, с которых комбо начинается
 */
class ComboGraph private constructor(val nodes: List<ComboNode>, val start: List<ComboNode>) {

    /** Все умения комбо. */
    val skills: Set<Int> = nodes.mapTo(mutableSetOf()) { it.skillNo }

    /** Шаг по номеру. */
    operator fun get(id: Int): ComboNode = nodes[id]

    companion object {
        /** Строит граф комбо [combo]. */
        fun of(combo: Combo): ComboGraph {
            val nodes = mutableListOf<ComboNode>()
            val entries = mutableMapOf<Int, MutableList<ComboNode>>()
            val exits = mutableMapOf<Int, MutableList<ComboNode>>()
            combo.columns.forEachIndexed { column, col ->
                col.lanes.forEachIndexed { lane, cells ->
                    var prev: ComboNode? = null
                    cells.forEachIndexed { index, cell ->
                        val skill = cell.skillNo ?: return@forEachIndexed
                        val at = CellAt(column, lane, index)
                        val node = ComboNode(nodes.size, skill, at, col is ComboColumn.Fork, cell.chain, combo.label(at))
                        nodes += node
                        prev?.let { link(it, node) } ?: entries.getOrPut(column) { mutableListOf() }.add(node)
                        prev = node
                    }
                    prev?.let { exits.getOrPut(column) { mutableListOf() }.add(it) }
                }
            }
            val live = entries.keys.sorted()
            if (live.isEmpty()) return ComboGraph(nodes, emptyList())
            val loop = live.first()
            live.forEachIndexed { k, column ->
                val target = entries.getValue(live.getOrNull(k + 1) ?: loop)
                exits[column].orEmpty().forEach { a -> target.forEach { b -> link(a, b) } }
            }
            return ComboGraph(nodes, entries.getValue(loop))
        }

        private fun link(a: ComboNode, b: ComboNode) {
            if (b in a.next) return
            a.next += b
            b.previous += a
        }
    }
}
