package com.layzen.bdotools.feature.portraits.presentation

import com.layzen.bdotools.feature.portraits.domain.BadgeIcon
import com.layzen.bdotools.feature.portraits.domain.BadgeSlot
import com.layzen.bdotools.feature.portraits.resources.Res
import com.layzen.bdotools.feature.portraits.resources.badge_barter
import com.layzen.bdotools.feature.portraits.resources.badge_fisher
import com.layzen.bdotools.feature.portraits.resources.badge_gatherer
import com.layzen.bdotools.feature.portraits.resources.badge_horse_breeder
import com.layzen.bdotools.feature.portraits.resources.badge_sailor
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_barter
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_fisher
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_gatherer
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_horse_breeder
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_sailor
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_slot_left
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_slot_right
import com.layzen.bdotools.feature.portraits.resources.portraits_badge_slot_top
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** SVG значка. Новый значок: файл в `composeResources/drawable`, строка в `strings.xml` и ветка здесь. */
internal val BadgeIcon.drawable: DrawableResource
    get() = when (this) {
        BadgeIcon.Fisher -> Res.drawable.badge_fisher
        BadgeIcon.HorseBreeder -> Res.drawable.badge_horse_breeder
        BadgeIcon.Gatherer -> Res.drawable.badge_gatherer
        BadgeIcon.Sailor -> Res.drawable.badge_sailor
        BadgeIcon.Barter -> Res.drawable.badge_barter
    }

/** Название значка в списках выбора. */
internal val BadgeIcon.label: StringResource
    get() = when (this) {
        BadgeIcon.Fisher -> Res.string.portraits_badge_fisher
        BadgeIcon.HorseBreeder -> Res.string.portraits_badge_horse_breeder
        BadgeIcon.Gatherer -> Res.string.portraits_badge_gatherer
        BadgeIcon.Sailor -> Res.string.portraits_badge_sailor
        BadgeIcon.Barter -> Res.string.portraits_badge_barter
    }

/** Название места значка в окне значков. */
internal val BadgeSlot.label: StringResource
    get() = when (this) {
        BadgeSlot.Top -> Res.string.portraits_badge_slot_top
        BadgeSlot.Left -> Res.string.portraits_badge_slot_left
        BadgeSlot.Right -> Res.string.portraits_badge_slot_right
    }
