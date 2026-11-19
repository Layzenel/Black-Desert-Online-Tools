package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Комбо: колонки слева направо и куда вернуться после последней.
 *
 * @property id постоянный номер комбо в хранилище
 * @property name название, которое видит игрок
 * @property classKey ключ класса (`CharacterClass.key`), чьи умения в комбо
 * @property columns колонки по порядку
 * @property fillers умения, которые оверлей предлагает, пока ждёт перезарядку следующего шага
 * @property vertical схема комбо показывает колонки сверху вниз, а ветки развилок — рядом
 * @property overlayStyle вид трекера поверх игры
 */
data class Combo(
    val id: String,
    val name: String,
    val classKey: String,
    val columns: List<ComboColumn> = emptyList(),
    val fillers: List<Int> = emptyList(),
    val vertical: Boolean = false,
    val overlayStyle: OverlayStyle = OverlayStyle.Next,
) {
    /** Номер первой колонки каждой колонки комбо (развилка шириной 2 сдвигает следующие на 2), с единицы. */
    fun columnStarts(): List<Int> = columns.runningFold(1) { start, column -> start + column.width }.dropLast(1)

    /** Подпись ячейки как в редакторе: «3» у обычной колонки, «4.2» — вторая ветка развилки. */
    fun label(at: CellAt): String {
        val start = columnStarts()[at.column] + at.index
        return if (columns[at.column] is ComboColumn.Fork) "$start.${at.lane + 1}" else "$start"
    }

    /** Ячейка по адресу [at] или `null`, если её нет. */
    operator fun get(at: CellAt): ComboCell? = columns.getOrNull(at.column)?.lanes?.getOrNull(at.lane)?.getOrNull(at.index)
}
