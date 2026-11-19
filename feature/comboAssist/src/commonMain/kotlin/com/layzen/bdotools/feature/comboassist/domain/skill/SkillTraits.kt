package com.layzen.bdotools.feature.comboassist.domain.skill

import com.layzen.bdotools.core.skill.domain.CrowdControl

/**
 * Что о умении известно из его подробностей: для списка филлеров, поиска по клавишам и щитов защиты на слотах.
 *
 * @property damage наносит ли урон
 * @property keyActions действия клавиш из команды умения (`Attack1`, `MoveFront`…) — по ним ищут умение
 * @property protections защита во время умения: устойчивость, защита спереди, неуязвимость
 * @property controls контроли при попадании, работающие в PvP
 */
data class SkillTraits(
    val damage: Boolean,
    val keyActions: Set<String>,
    val protections: Set<SkillProtection> = emptySet(),
    val controls: Set<CrowdControl> = emptySet(),
)
