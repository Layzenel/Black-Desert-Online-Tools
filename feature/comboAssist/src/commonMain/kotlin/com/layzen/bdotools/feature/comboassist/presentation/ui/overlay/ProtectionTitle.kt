package com.layzen.bdotools.feature.comboassist.presentation.ui.overlay

import com.layzen.bdotools.feature.comboassist.domain.skill.SkillProtection
import com.layzen.bdotools.feature.comboassist.resources.Res
import com.layzen.bdotools.feature.comboassist.resources.combo_protection_forward_guard
import com.layzen.bdotools.feature.comboassist.resources.combo_protection_invincible
import com.layzen.bdotools.feature.comboassist.resources.combo_protection_super_armor

/** Название защиты: в подсказке над щитом на слоте. */
internal val SkillProtection.title
    get() = when (this) {
        SkillProtection.SuperArmor -> Res.string.combo_protection_super_armor
        SkillProtection.ForwardGuard -> Res.string.combo_protection_forward_guard
        SkillProtection.Invincible -> Res.string.combo_protection_invincible
    }
