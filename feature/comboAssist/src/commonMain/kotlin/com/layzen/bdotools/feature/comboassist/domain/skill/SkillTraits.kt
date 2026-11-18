package com.layzen.bdotools.feature.comboassist.domain.skill

/**
 * Что о умении известно из его подробностей: для списка филлеров и поиска по клавишам.
 *
 * @property damage наносит ли урон
 * @property keyActions действия клавиш из команды умения (`Attack1`, `MoveFront`…) — по ним ищут умение
 */
data class SkillTraits(val damage: Boolean, val keyActions: Set<String>)
