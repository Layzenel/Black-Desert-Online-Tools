package com.layzen.bdotools.feature.uieditor.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals

class SkillEffectsTest {

    private fun details(vararg lines: List<String>) = SkillDetails(
        mapOf("en" to SkillText("", "", 0, 0, 0, 0, 0, "", lines.toList())),
    )

    @Test
    fun readsDefensesOnceEachInEnumOrderWithTheirLines() {
        val effects = SkillEffects.of(
            details(
                listOf("Super Armor -> <span style=\"color: #e9bd23\">Invincible</span> -> Super Armor during the skill"),
                listOf("Forward Guard", "(Nullified during cooldown) "),
                listOf("Damage Reduction +50% during Super Armor."),
                listOf("Super Armor while moving"),
            ),
        )

        assertEquals(
            listOf(
                SkillDefense(DefenseEffect.SuperArmor, listOf(0, 3)),
                SkillDefense(DefenseEffect.Invincible, listOf(0)),
                SkillDefense(DefenseEffect.ForwardGuard, listOf(1)),
            ),
            effects.defenses,
        )
    }

    @Test
    fun controlIsPveOnlyOnlyWhenEveryLineSaysSo() {
        val effects = SkillEffects.of(
            details(
                listOf("Bound on hits", "(PvE only) "),
                listOf("Bound on attack 2 hits"),
                listOf("Floating on hits", "(PvE only) ", "(Nullified during cooldown) "),
                listOf("Stiffness before attack"),
                listOf("Stun Resistance +10%"),
                listOf("Pull in the target on hits"),
            ),
        )

        assertEquals(
            listOf(
                SkillControl(ControlType.Bound, pveOnly = false, lines = listOf(0, 1)),
                SkillControl(ControlType.Stiffness, pveOnly = false, lines = listOf(3)),
                SkillControl(ControlType.Floating, pveOnly = true, lines = listOf(2)),
            ),
            effects.controls,
        )
        assertEquals(listOf(ControlType.Bound, ControlType.Stiffness), effects.pvpControls.map { it.type })
    }

    @Test
    fun noEnglishTextMeansNoEffects() {
        assertEquals(SkillEffects(emptyList(), emptyList()), SkillEffects.of(SkillDetails(emptyMap())))
    }
}
