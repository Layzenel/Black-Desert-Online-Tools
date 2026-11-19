package com.layzen.bdotools.uikit.icons

import com.layzen.bdotools.core.skill.domain.CrowdControl
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.control_bound
import com.layzen.bdotools.uikit.resources.control_float
import com.layzen.bdotools.uikit.resources.control_freeze
import com.layzen.bdotools.uikit.resources.control_knockback
import com.layzen.bdotools.uikit.resources.control_knockdown
import com.layzen.bdotools.uikit.resources.control_stiff
import com.layzen.bdotools.uikit.resources.control_stun
import com.layzen.bdotools.uikit.resources.crowd_control_title_stun
import com.layzen.bdotools.uikit.resources.crowd_control_title_knockdown
import com.layzen.bdotools.uikit.resources.crowd_control_title_bound
import com.layzen.bdotools.uikit.resources.crowd_control_title_stiffness
import com.layzen.bdotools.uikit.resources.crowd_control_title_floating
import com.layzen.bdotools.uikit.resources.crowd_control_title_knockback
import com.layzen.bdotools.uikit.resources.crowd_control_title_freezing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** Значок контроля, как в игре: у навыков в редакторе интерфейса и под слотами перезарядки. */
val CrowdControl.icon: DrawableResource
    get() = when (this) {
        CrowdControl.Stun -> Res.drawable.control_stun
        CrowdControl.Knockdown -> Res.drawable.control_knockdown
        CrowdControl.Bound -> Res.drawable.control_bound
        CrowdControl.Stiffness -> Res.drawable.control_stiff
        CrowdControl.Floating -> Res.drawable.control_float
        CrowdControl.Knockback -> Res.drawable.control_knockback
        CrowdControl.Freezing -> Res.drawable.control_freeze
    }

/** Название контроля, как в клиенте игры. */
val CrowdControl.title: StringResource
    get() = when (this) {
        CrowdControl.Stun -> Res.string.crowd_control_title_stun
        CrowdControl.Knockdown -> Res.string.crowd_control_title_knockdown
        CrowdControl.Bound -> Res.string.crowd_control_title_bound
        CrowdControl.Stiffness -> Res.string.crowd_control_title_stiffness
        CrowdControl.Floating -> Res.string.crowd_control_title_floating
        CrowdControl.Knockback -> Res.string.crowd_control_title_knockback
        CrowdControl.Freezing -> Res.string.crowd_control_title_freezing
    }
