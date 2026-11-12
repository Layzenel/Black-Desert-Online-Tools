package com.layzen.bdotools.feature.uieditor.presentation.ui.skill

import com.layzen.bdotools.feature.uieditor.domain.skill.ControlType
import com.layzen.bdotools.feature.uieditor.domain.skill.DefenseEffect
import com.layzen.bdotools.feature.uieditor.resources.Res
import com.layzen.bdotools.feature.uieditor.resources.control_bound
import com.layzen.bdotools.feature.uieditor.resources.control_float
import com.layzen.bdotools.feature.uieditor.resources.control_freeze
import com.layzen.bdotools.feature.uieditor.resources.control_knockback
import com.layzen.bdotools.feature.uieditor.resources.control_knockdown
import com.layzen.bdotools.feature.uieditor.resources.control_stiff
import com.layzen.bdotools.feature.uieditor.resources.control_stun
import com.layzen.bdotools.feature.uieditor.resources.defense_forward_guard
import com.layzen.bdotools.feature.uieditor.resources.defense_invincible
import com.layzen.bdotools.feature.uieditor.resources.defense_super_armor
import com.layzen.bdotools.feature.uieditor.resources.slots_control_bound
import com.layzen.bdotools.feature.uieditor.resources.slots_control_floating
import com.layzen.bdotools.feature.uieditor.resources.slots_control_freezing
import com.layzen.bdotools.feature.uieditor.resources.slots_control_knockback
import com.layzen.bdotools.feature.uieditor.resources.slots_control_knockdown
import com.layzen.bdotools.feature.uieditor.resources.slots_control_stiffness
import com.layzen.bdotools.feature.uieditor.resources.slots_control_stun
import com.layzen.bdotools.feature.uieditor.resources.slots_defense_forward_guard
import com.layzen.bdotools.feature.uieditor.resources.slots_defense_invincible
import com.layzen.bdotools.feature.uieditor.resources.slots_defense_super_armor
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Иконка контроля, как в игре. */
internal val ControlType.icon: DrawableResource
    get() = when (this) {
        ControlType.Stun -> Res.drawable.control_stun
        ControlType.Knockdown -> Res.drawable.control_knockdown
        ControlType.Bound -> Res.drawable.control_bound
        ControlType.Stiffness -> Res.drawable.control_stiff
        ControlType.Floating -> Res.drawable.control_float
        ControlType.Knockback -> Res.drawable.control_knockback
        ControlType.Freezing -> Res.drawable.control_freeze
    }

/** Название контроля, как в клиенте игры. */
internal val ControlType.title: StringResource
    get() = when (this) {
        ControlType.Stun -> Res.string.slots_control_stun
        ControlType.Knockdown -> Res.string.slots_control_knockdown
        ControlType.Bound -> Res.string.slots_control_bound
        ControlType.Stiffness -> Res.string.slots_control_stiffness
        ControlType.Floating -> Res.string.slots_control_floating
        ControlType.Knockback -> Res.string.slots_control_knockback
        ControlType.Freezing -> Res.string.slots_control_freezing
    }

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
