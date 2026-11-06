package com.layzen.bdotools.feature.uieditor.domain

/**
 * Отдаление групп выбранных элементов по одной оси, UI-px: [value] — сейчас, [min] — ближе группы сойдутся
 * вплотную и сольются в одну. Считает [groupSpread].
 */
data class SpreadAxis(val value: Int, val min: Int)
