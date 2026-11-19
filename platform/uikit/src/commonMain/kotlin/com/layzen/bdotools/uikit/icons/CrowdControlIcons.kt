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
import org.jetbrains.compose.resources.DrawableResource

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
