package com.layzen.bdotools.feature.comboassist.domain.combo

/**
 * Ячейка комбо: одно умение.
 *
 * @property skillNo номер навыка (`SkillNo`); `null` — пустая ячейка, оверлей её пропускает
 * @property chain связка: жать сразу после предыдущего умения, иначе цепочка сорвётся
 * @property optional опциональный шаг: оверлей его не подсказывает и обходит, но если игрок его применил —
 *   комбо продолжается с него
 */
data class ComboCell(
    val skillNo: Int? = null,
    val chain: Boolean = false,
    val optional: Boolean = false,
)
