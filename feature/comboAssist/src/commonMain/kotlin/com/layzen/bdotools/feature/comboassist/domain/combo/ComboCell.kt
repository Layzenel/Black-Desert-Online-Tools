package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Ячейка комбо: одно умение.
 *
 * @property skillNo номер навыка (`SkillNo`); `null` — пустая ячейка, оверлей её пропускает
 * @property chain связка: жать сразу после предыдущего умения, иначе цепочка сорвётся
 * @property note пометка игрока к шагу («в прыжке», «после рывка»): трекер показывает её, когда шаг — следующий
 */
data class ComboCell(
    val skillNo: Int? = null,
    val chain: Boolean = false,
    val note: String = "",
)
