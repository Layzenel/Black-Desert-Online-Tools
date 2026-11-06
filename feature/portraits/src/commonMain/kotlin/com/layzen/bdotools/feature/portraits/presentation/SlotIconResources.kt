package com.layzen.bdotools.feature.portraits.presentation

import com.layzen.bdotools.feature.portraits.domain.BadgeGroup
import com.layzen.bdotools.feature.portraits.domain.SlotIcon
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.badge_world_boss
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_world_boss
import com.layzen.bdotools.uikit.icons.icon
import com.layzen.bdotools.uikit.icons.title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Все иконки, которые можно поставить значком: их загружает отрисовщик. */
internal val allSlotIcons: List<SlotIcon> = BadgeGroup.entries.flatMap { it.icons }

/** SVG значка. */
internal val SlotIcon.drawable: DrawableResource
    get() = when (this) {
        SlotIcon.WorldBoss -> Res.drawable.badge_world_boss
        is SlotIcon.Skill -> skill.icon
        is SlotIcon.Class -> characterClass.icon
    }

/** Название значка для списка выбора. */
internal val SlotIcon.label: StringResource
    get() = when (this) {
        SlotIcon.WorldBoss -> Res.string.portraits_badge_world_boss
        is SlotIcon.Skill -> skill.title
        is SlotIcon.Class -> characterClass.title
    }
