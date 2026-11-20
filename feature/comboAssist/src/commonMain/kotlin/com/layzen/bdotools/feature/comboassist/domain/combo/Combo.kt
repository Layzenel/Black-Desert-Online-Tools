package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Комбо: колонки слева направо и куда вернуться после последней.
 *
 * @property id постоянный номер комбо в хранилище
 * @property name название, которое видит игрок
 * @property classKey ключ класса (`CharacterClass.key`), чьи умения в комбо
 * @property columns колонки по порядку
 * @property fillers умения, которые оверлей предлагает, пока ждёт перезарядку следующего шага
 * @property pool умения, которые трекер показывает всегда, отдельно от шагов: готовое — в золотой рамке, на перезарядке —
 *   с секундами (защита, баф, отскок — что жать по ситуации)
 * @property keyTracked умения, которых нет ни в истории умений игры, ни на слотах перезарядок: трекер узнаёт их по клавишам
 *   и расходу ресурса. Общие для обычного комбо и комбо под гневом
 * @property vertical схема комбо показывает колонки сверху вниз, а ветки развилок — рядом
 * @property overlayStyle вид трекера поверх игры
 * @property rage комбо под гневом Чёрного духа или `null`, если его нет
 */
data class Combo(
    val id: String,
    val name: String,
    val classKey: String,
    val columns: List<ComboColumn> = emptyList(),
    val fillers: List<Int> = emptyList(),
    val pool: List<Int> = emptyList(),
    val keyTracked: Set<Int> = emptySet(),
    val vertical: Boolean = false,
    val overlayStyle: OverlayStyle = OverlayStyle.Next,
    val rage: ComboRage? = null,
) {
    /**
     * Часть комбо, которую правят или по которой ведут трекер: обычное комбо или, если [rageShown], комбо под
     * гневом в виде такого же комбо (без своего [rage]). Под гневом, когда его нет, — `null`.
     */
    fun part(rageShown: Boolean): Combo? =
        if (!rageShown) this else rage?.let { copy(columns = it.columns, fillers = it.fillers, pool = it.pool, rage = null) }

    /** Комбо, в котором часть [rageShown] заменена правленой [edited] (результатом правки [part]). */
    fun withPart(rageShown: Boolean, edited: Combo): Combo =
        if (rageShown) copy(rage = ComboRage(edited.columns, edited.fillers, edited.pool), keyTracked = edited.keyTracked) else edited.copy(rage = rage)

    /** Обычное комбо и комбо под гневом, если оно есть. */
    fun parts(): List<Combo> = listOfNotNull(part(false), part(true))

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
