package com.layzen.bdotools.feature.portraits.presentation

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.game.LifeSkill
import com.layzen.bdotools.feature.portraits.domain.SlotIcon
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.icons.title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Все иконки, которые можно поставить значком: их загружает отрисовщик. */
internal val allSlotIcons: List<SlotIcon> =
    LifeSkill.entries.map(SlotIcon::Skill) + CharacterClass.entries.map(SlotIcon::Class)

/** SVG значка. */
internal val SlotIcon.drawable: DrawableResource
    get() = when (this) {
        is SlotIcon.Skill -> skill.icon
        is SlotIcon.Class -> characterClass.icon
    }

/** Название значка для списка выбора. */
internal val SlotIcon.label: StringResource
    get() = when (this) {
        is SlotIcon.Skill -> skill.title
        is SlotIcon.Class -> characterClass.title
    }
