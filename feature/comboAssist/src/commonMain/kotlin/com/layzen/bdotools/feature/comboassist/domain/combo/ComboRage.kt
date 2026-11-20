package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Комбо под гневом Чёрного духа: пока висят три бафа гнева, у некоторых классов комбо другое. Класс, название
 * и вид трекера — общие с обычным комбо.
 *
 * @property columns колонки по порядку
 * @property fillers умения, которые оверлей предлагает, пока ждёт перезарядку
 * @property pool умения, которые трекер показывает всегда ([Combo.pool])
 */
data class ComboRage(val columns: List<ComboColumn>, val fillers: List<Int> = emptyList(), val pool: List<Int> = emptyList()) {
    companion object {
        /** Пустое комбо под гневом: одна пустая ячейка. */
        val Empty = ComboRage(listOf(ComboColumn.Single(ComboCell())))
    }
}
