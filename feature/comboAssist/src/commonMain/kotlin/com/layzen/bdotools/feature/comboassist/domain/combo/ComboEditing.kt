package com.layzen.bdotools.feature.comboassist.domain.combo

import com.layzen.bdotools.feature.comboassist.domain.skill.ComboSkills

/**
 * Правка комбо в редакторе. Каждая операция возвращает новое комбо и ячейку, которую выбрать после неё.
 *
 * @property combo комбо после правки
 * @property selection выбранная ячейка; `null` — ничего не выбрано
 */
data class ComboEdit(val combo: Combo, val selection: CellAt?)

/** Вставляет пустую колонку перед колонкой [index] и выбирает её. */
fun Combo.insertColumn(index: Int): ComboEdit {
    val columns = columns.toMutableList().apply { add(index, ComboColumn.Single(ComboCell())) }
    return ComboEdit(copy(columns = columns), CellAt(index))
}

/** Делает обычную колонку [column] развилкой: её умение — первая ветка, вторая ветка пустая и выбирается. */
fun Combo.toFork(column: Int): ComboEdit {
    val single = columns[column] as? ComboColumn.Single ?: return ComboEdit(this, null)
    return ComboEdit(replace(column, ComboColumn.Fork(listOf(listOf(single.cell), listOf(ComboCell())))), CellAt(column, 1))
}

/** Добавляет пустую ветку в развилку [column] и выбирает её. */
fun Combo.addLane(column: Int): ComboEdit {
    val fork = columns[column] as? ComboColumn.Fork ?: return ComboEdit(this, null)
    return ComboEdit(replace(column, fork.copy(lanes = fork.lanes + listOf(listOf(ComboCell())))), CellAt(column, fork.lanes.size))
}

/** Удлиняет ветку [lane] развилки [column] пустой ячейкой и выбирает её. */
fun Combo.growLane(column: Int, lane: Int): ComboEdit {
    val fork = columns[column] as? ComboColumn.Fork ?: return ComboEdit(this, null)
    val lanes = fork.lanes.mapIndexed { i, cells -> if (i == lane) cells + ComboCell() else cells }
    return ComboEdit(replace(column, fork.copy(lanes = lanes)), CellAt(column, lane, lanes[lane].size - 1))
}

/** Меняет местами ячейки [a] и [b]: связка и опциональность едут вместе с умением. */
fun Combo.swap(a: CellAt, b: CellAt): ComboEdit {
    val first = this[a] ?: return ComboEdit(this, a)
    val second = this[b] ?: return ComboEdit(this, a)
    return ComboEdit(put(a, second).put(b, first), b)
}

/**
 * Удаляет ячейку [at]. Пустая ветка пропадает, развилка из одной ветки становится обычными колонками,
 * пустая колонка — удаляется.
 */
fun Combo.removeCell(at: CellAt): ComboEdit {
    val result = when (val column = columns.getOrNull(at.column)) {
        null -> return ComboEdit(this, null)
        is ComboColumn.Single -> columns.filterIndexed { i, _ -> i != at.column }
        is ComboColumn.Fork -> {
            val lanes = column.lanes.mapIndexed { i, cells -> if (i == at.lane) cells.filterIndexed { j, _ -> j != at.index } else cells }
                .filter { it.isNotEmpty() }
            val replacement = when (lanes.size) {
                0 -> emptyList()
                1 -> lanes.single().map { ComboColumn.Single(it) }
                else -> listOf(column.copy(lanes = lanes))
            }
            columns.take(at.column) + replacement + columns.drop(at.column + 1)
        }
    }
    return ComboEdit(copy(columns = result), null)
}

/**
 * Ставит умение [skillNo] в ячейку [at]. Связку ставит сама, если по данным умение — продолжение одного из
 * предыдущих. Если ячейка последняя в ветке или в комбо, после неё появляется пустая и выбирается; иначе
 * выбирается следующая ячейка.
 */
fun Combo.fill(at: CellAt, skillNo: Int, skills: ComboSkills): ComboEdit {
    val old = this[at] ?: ComboCell()
    val skill = skills[skillNo]
    val chain = skill.onlyAfter && previousSkills(at).any { it in skill.after }
    val filled = put(at, old.copy(skillNo = skillNo, chain = chain))
    val column = filled.columns[at.column]
    return when {
        column is ComboColumn.Fork && at.index == column.lanes[at.lane].lastIndex -> filled.growLane(at.column, at.lane)
        column is ComboColumn.Single && at.column == filled.columns.lastIndex -> filled.insertColumn(filled.columns.size)
        else -> ComboEdit(filled, filled.nextCell(at))
    }
}

/** Ячейка после [at]: следующая в ветке или первая в следующей колонке. */
fun Combo.nextCell(at: CellAt): CellAt? {
    val column = columns.getOrNull(at.column) ?: return null
    if (column is ComboColumn.Fork && at.index + 1 < column.lanes[at.lane].size) return at.copy(index = at.index + 1)
    return if (at.column + 1 < columns.size) CellAt(at.column + 1) else null
}

/** Умения, после которых стоит ячейка [at]: предыдущая в ветке или последние ячейки предыдущей колонки (по кругу). */
fun Combo.previousSkills(at: CellAt): List<Int> {
    if (at.index > 0) return listOfNotNull(this[at.copy(index = at.index - 1)]?.skillNo)
    val previous = columns.getOrNull(at.column - 1) ?: columns.takeIf { it.size > 1 && at.column == 0 }?.last() ?: return emptyList()
    return previous.lanes.mapNotNull { it.lastOrNull()?.skillNo }
}

/** Меняет ячейку [at], сохраняя остальные. */
fun Combo.put(at: CellAt, cell: ComboCell): Combo = replace(
    at.column,
    when (val column = columns[at.column]) {
        is ComboColumn.Single -> ComboColumn.Single(cell)
        is ComboColumn.Fork -> column.copy(lanes = column.lanes.mapIndexed { i, cells -> if (i == at.lane) cells.mapIndexed { j, c -> if (j == at.index) cell else c } else cells })
    },
)

/** Все ячейки комбо с адресами. */
fun Combo.cells(): List<Pair<CellAt, ComboCell>> = columns.flatMapIndexed { column, col ->
    col.lanes.flatMapIndexed { lane, cells -> cells.mapIndexed { index, cell -> CellAt(column, lane, index) to cell } }
}

private fun Combo.replace(index: Int, column: ComboColumn): Combo =
    copy(columns = columns.mapIndexed { i, c -> if (i == index) column else c })

/** Связывает развилку [column] с предыдущей развилкой или снимает связь; выбранная ячейка не меняется. */
fun Combo.toggleLinked(column: Int, selection: CellAt?): ComboEdit {
    val fork = columns.getOrNull(column) as? ComboColumn.Fork ?: return ComboEdit(this, selection)
    return ComboEdit(replace(column, fork.copy(linked = !fork.linked)), selection)
}

/**
 * Вставляет пустую ячейку слева от [at] или, при [after], справа и выбирает её. В ветке развилки ячейка встаёт
 * в ту же ветку, у обычной колонки — новой колонкой.
 */
fun Combo.insertCell(at: CellAt, after: Boolean): ComboEdit = when (val column = columns.getOrNull(at.column)) {
    null -> ComboEdit(this, at)
    is ComboColumn.Single -> insertColumn(if (after) at.column + 1 else at.column)
    is ComboColumn.Fork -> {
        val index = if (after) at.index + 1 else at.index
        val lanes = column.lanes.mapIndexed { i, cells -> if (i == at.lane) cells.toMutableList().apply { add(index, ComboCell()) } else cells }
        ComboEdit(replace(at.column, column.copy(lanes = lanes)), at.copy(index = index))
    }
}
