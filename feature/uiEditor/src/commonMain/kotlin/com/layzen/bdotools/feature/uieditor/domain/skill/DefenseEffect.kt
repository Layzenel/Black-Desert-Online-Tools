package com.layzen.bdotools.feature.uieditor.domain.skill

/** Эффект защиты во время навыка; [tooltipKey] — как он пишется в английском тултипе. */
enum class DefenseEffect(val tooltipKey: String) {
    SuperArmor("Super Armor"),
    Invincible("Invincible"),
    ForwardGuard("Forward Guard"),
}
