package com.layzen.bdotools.feature.comboassist.domain.keys

/**
 * Как применить умение клавишами и видно ли применение по ресурсу.
 *
 * @property variants варианты команды — действия игры, зажатые вместе ([keyVariants])
 * @property visibleSpend умение тратит заметно ресурса класса (MP в данных bdolytics): нажатие засчитывается, только когда
 *   полоска ресурса просела, — так нажатие, на которое игра не ответила, шаг не засчитывает. Дешёвое умение — сразу по нажатию
 */
data class KeyCommand(val variants: List<Set<String>>, val visibleSpend: Boolean)
