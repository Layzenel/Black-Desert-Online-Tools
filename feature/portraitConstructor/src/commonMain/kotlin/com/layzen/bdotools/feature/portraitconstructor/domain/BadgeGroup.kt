package com.layzen.bdotools.feature.portraitconstructor.domain

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.game.LifeSkill

/** Группы в списке выбора значка: классы персонажей и функциональные — мировой босс первым, затем жизненные навыки. */
enum class BadgeGroup {
    Class,
    Functional,
    ;

    val icons: List<SlotIcon>
        get() = when (this) {
            Class -> CharacterClass.entries.map(SlotIcon::Class)
            Functional -> listOf(SlotIcon.WorldBoss) + LifeSkill.entries.map(SlotIcon::Skill)
        }
}
