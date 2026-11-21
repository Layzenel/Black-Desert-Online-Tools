package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Ячейка комбо: одно умение.
 *
 * @property skillNo номер навыка (`SkillNo`); `null` — пустая ячейка, оверлей её пропускает
 * @property chain связка: жать сразу после предыдущего умения, иначе цепочка сорвётся
 * @property note пометка игрока к шагу («в прыжке», «после рывка»): трекер показывает её, когда шаг — следующий
 * @property keys какой командой игрок применяет умение в этом шаге (действия игры, зажатые вместе), если не любой из
 *   вариантов описания: её показывает трекер и по ней слушает клавиши. Одно умение в разных шагах жмут по-разному
 */
data class ComboCell(
    val skillNo: Int? = null,
    val chain: Boolean = false,
    val note: String = "",
    val keys: Set<String>? = null,
)
