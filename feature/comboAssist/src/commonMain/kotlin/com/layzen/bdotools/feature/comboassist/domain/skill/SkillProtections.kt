package com.layzen.bdotools.feature.comboassist.domain.skill

private val TAG = Regex("<[^>]*>")
private val SUPER_ARMOR = Regex("""super\s*armor""", RegexOption.IGNORE_CASE)
private val FORWARD_GUARD = Regex("""forward\s*guard""", RegexOption.IGNORE_CASE)
private val INVINCIBLE = Regex("""invincib""", RegexOption.IGNORE_CASE)

// защита только от монстров игроку в PvP не поможет — такие строки пропускаются
private val MONSTERS_ONLY = Regex("""pve\s*only|monsters?\s*only|against\s*monsters""", RegexOption.IGNORE_CASE)

/**
 * Защита умения по строкам эффектов его английского тултипа («Super Armor during the skill», «Forward Guard»,
 * «Invincible during the skill»). Английский текст одинаков для игроков на любом языке. Учитывается только защита,
 * работающая в PvP: строка с пометкой «(PvE only)» пропускается целиком.
 */
fun skillProtections(effects: List<String>): Set<SkillProtection> = buildSet {
    effects.map { it.replace(TAG, "") }.filterNot { MONSTERS_ONLY.containsMatchIn(it) }.forEach { line ->
        if (SUPER_ARMOR.containsMatchIn(line)) add(SkillProtection.SuperArmor)
        if (FORWARD_GUARD.containsMatchIn(line)) add(SkillProtection.ForwardGuard)
        if (INVINCIBLE.containsMatchIn(line)) add(SkillProtection.Invincible)
    }
}
