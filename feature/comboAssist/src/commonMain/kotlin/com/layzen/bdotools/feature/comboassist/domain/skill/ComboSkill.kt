package com.layzen.bdotools.feature.comboassist.domain.skill

/**
 * Что комбо нужно знать об умении.
 *
 * @property skillNo номер навыка
 * @property cooldownMs перезарядка в миллисекундах; 0 — без перезарядки
 * @property after умения, сразу после которых это можно применить (связки из команды навыка)
 * @property onlyAfter продолжение связки: без одного из [after] умение не применить
 */
data class ComboSkill(
    val skillNo: Int,
    val cooldownMs: Long = 0,
    val after: Set<Int> = emptySet(),
    val onlyAfter: Boolean = false,
)
