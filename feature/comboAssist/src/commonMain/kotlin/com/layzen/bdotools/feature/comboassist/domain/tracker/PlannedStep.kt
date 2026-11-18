package com.layzen.bdotools.feature.comboassist.domain.tracker

import com.layzen.bdotools.feature.comboassist.domain.graph.ComboNode

/**
 * Шаг подсказки: какое умение жать и когда.
 *
 * @property skillNo умение
 * @property atMs момент, когда его можно применить (с учётом перезарядки)
 * @property node шаг комбо; `null` у входа в оборванную связку — этого умения в комбо нет
 */
data class PlannedStep(val skillNo: Int, val atMs: Long, val node: ComboNode?) {
    /** Вход в оборванную связку через другое умение. */
    val isEntry: Boolean get() = node == null
}
