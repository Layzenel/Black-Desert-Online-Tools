package com.layzen.bdotools.feature.portraits.domain

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.game.LifeSkill

/** Группы в списке выбора значка: классы персонажей и функциональные — жизненные навыки. */
enum class BadgeGroup {
    Class,
    Functional,
    ;

    val icons: List<SlotIcon>
        get() = when (this) {
            Class -> CharacterClass.entries.map(SlotIcon::Class)
            Functional -> LifeSkill.entries.map(SlotIcon::Skill)
        }
}
