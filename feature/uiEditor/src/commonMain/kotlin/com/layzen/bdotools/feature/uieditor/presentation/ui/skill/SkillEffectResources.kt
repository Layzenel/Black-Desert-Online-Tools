package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import com.layzen.bdotools.feature.uieditor.domain.skill.DefenseEffect
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.defense_forward_guard
import com.layzen.bdotools.feature.uieditor.resources.defense_invincible
import com.layzen.bdotools.feature.uieditor.resources.defense_super_armor
import com.layzen.bdotools.feature.uieditor.resources.slots_defense_forward_guard
import com.layzen.bdotools.feature.uieditor.resources.slots_defense_invincible
import com.layzen.bdotools.feature.uieditor.resources.slots_defense_super_armor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Иконка эффекта защиты, как в игре. */
internal val DefenseEffect.icon: DrawableResource
    get() = when (this) {
        DefenseEffect.SuperArmor -> Res.drawable.defense_super_armor
        DefenseEffect.Invincible -> Res.drawable.defense_invincible
        DefenseEffect.ForwardGuard -> Res.drawable.defense_forward_guard
    }

/** Название эффекта защиты, как в клиенте игры. */
internal val DefenseEffect.title: StringResource
    get() = when (this) {
        DefenseEffect.SuperArmor -> Res.string.slots_defense_super_armor
        DefenseEffect.Invincible -> Res.string.slots_defense_invincible
        DefenseEffect.ForwardGuard -> Res.string.slots_defense_forward_guard
    }
