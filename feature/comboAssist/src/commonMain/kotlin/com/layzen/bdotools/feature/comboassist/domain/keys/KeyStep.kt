package com.layzen.bdotools.feature.comboassist.domain.keys

/**
 * Шаг комбо, который можно подтвердить нажатием клавиш: трекер ждёт его сейчас или сразу после [after].
 *
 * @property skillNo умение шага
 * @property after умение, которое трекер ждёт перед этим шагом (`null` — шаг ждут уже сейчас): строка истории
 *   предыдущего умения запаздывает, а продолжение связки жмут ещё во время него
 * @property variants варианты команды — действия игры, зажатые вместе (`{KeyBind:MoveFront}` + `{KeyBind:Attack1}`)
 * @property visibleSpend умение тратит заметно ресурса: нажатие ждёт, пока полоска ресурса просядет ([KeyCommand])
 */
data class KeyStep(val skillNo: Int, val after: Int?, val variants: List<Set<String>>, val visibleSpend: Boolean = false)
