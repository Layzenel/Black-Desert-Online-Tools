package com.layzen.bdotools.feature.comboassist.presentation.state

import com.layzen.bdotools.feature.comboassist.domain.tracker.TrackerEvent

/**
 * Строка журнала тренировки: событие трекера и подпись шага, где игрок после него. Текст собирает UI.
 *
 * @property id порядковый номер, ключ списка
 * @property event событие
 * @property where подпись шага после события («4.1») или `null`
 * @property onCooldown умение было на перезарядке и не вышло
 */
data class LogLine(val id: Long, val event: TrackerEvent?, val where: String?, val onCooldown: Int? = null)
