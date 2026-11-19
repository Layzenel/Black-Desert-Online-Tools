package com.layzen.bdotools.feature.comboassist.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals

class SkillProtectionsTest {

    @Test
    fun readsEachProtectionFromEffects() {
        assertEquals(setOf(SkillProtection.SuperArmor), skillProtections(listOf("<PAColor0xffd8ad70>Super Armor during the skill<PAOldColor>")))
        assertEquals(setOf(SkillProtection.ForwardGuard), skillProtections(listOf("Forward Guard during the skill")))
        assertEquals(setOf(SkillProtection.Invincible, SkillProtection.SuperArmor), skillProtections(listOf("Super Armor > Invincible > Normal when using the skill")))
    }

    @Test
    fun pveOnlyProtectionIsIgnored() {
        assertEquals(emptySet(), skillProtections(listOf("Invincible during the skill (PvE only)", "Super Armor (PvE only)", "1325% x2")))
    }
}
