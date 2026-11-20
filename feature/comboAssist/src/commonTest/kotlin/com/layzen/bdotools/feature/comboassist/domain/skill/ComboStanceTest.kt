package com.layzen.bdotools.feature.comboassist.domain.skill

import com.layzen.bdotools.core.game.CharacterClass
import com.layzen.bdotools.core.skill.domain.Skill
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComboStanceTest {
    private fun seraph(ru: String, en: String) = Skill(setOf("seraph"), mapOf("ru" to ru, "en" to en), "")

    @Test
    fun sacramentumHidesPurgatumSkills() {
        assertTrue(ComboStance.Sacramentum.allows(seraph("С: Перекрестный разрез I", "Sacra: Crossing Slash I")))
        assertFalse(ComboStance.Sacramentum.allows(seraph("П: Что-то", "Purga: Something")))
        assertTrue(ComboStance.Sacramentum.allows(seraph("Абсолют: Перекрестный разрез", "Absolute: Crossing Slash")))
    }

    @Test
    fun purgatumHidesSacramentumSkills() {
        assertFalse(ComboStance.Purgatum.allows(seraph("С: Перекрестный разрез I", "Sacra: Crossing Slash I")))
        assertTrue(ComboStance.Purgatum.allows(seraph("П: Что-то", "Purga: Something")))
    }

    @Test
    fun seraphHasOwnStances() {
        assertEquals(listOf(ComboStance.Sacramentum, ComboStance.Purgatum), ComboStance.of(CharacterClass.Seraph))
        assertEquals(emptyList(), ComboStance.of(CharacterClass.Scholar))
        assertEquals(listOf(ComboStance.Succession, ComboStance.Awakening), ComboStance.of(CharacterClass.Striker))
    }

    @Test
    fun stanceOfOtherClassIsReplaced() {
        assertEquals(ComboStance.Sacramentum, ComboStance.keep(CharacterClass.Seraph, ComboStance.Succession))
        assertNull(ComboStance.keep(CharacterClass.Scholar, ComboStance.Succession))
        assertEquals(ComboStance.Sacramentum, ComboStance.keep(CharacterClass.Seraph, null))
    }
}
