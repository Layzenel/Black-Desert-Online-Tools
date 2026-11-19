package com.layzen.bdotools.feature.comboassist.domain.skill

/**
 * Что о умении известно из его подробностей: для списка филлеров, поиска по клавишам и щитов защиты на слотах.
 *
 * @property damage наносит ли урон
 * @property keyActions действия клавиш из команды умения (`Attack1`, `MoveFront`…) — по ним ищут умение
 * @property protections защита во время умения: устойчивость, защита спереди, неуязвимость
 */
data class SkillTraits(val damage: Boolean, val keyActions: Set<String>, val protections: Set<SkillProtection> = emptySet())
