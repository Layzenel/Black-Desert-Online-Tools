package com.layzen.bdotools.feature.uieditor.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SkillCatalogTest {

    private fun skill(vararg classes: String) = Skill(classes.toSet(), mapOf("en" to "Skill", "ru" to "Навык"), "/i.webp")

    private val catalog = SkillCatalog(
        mapOf(
            1 to skill("wizard"),
            2 to skill("wizard", "witch"),
            3 to skill("witch"),
            4 to skill("wizard", "witch", "striker"),
        ),
    )

    @Test
    fun classIsCommonClassOfKnownSkills() {
        assertEquals("wizard", catalog.classOf(listOf(1, 2, 4, 999)))
        assertEquals("witch", catalog.classOf(listOf(3, 2)))
    }

    @Test
    fun onlySharedOrUnknownSkillsGiveNoClass() {
        assertNull(catalog.classOf(listOf(2, 4)))
        assertNull(catalog.classOf(listOf(999)))
        assertNull(catalog.classOf(emptyList()))
        assertNull(catalog.classOf(listOf(1, 3)))
    }

    @Test
    fun choicesAreMaxRanksOfClassAndStance() {
        fun skill(stance: SkillStance?, max: Boolean = true) = Skill(setOf("striker"), mapOf("en" to "S"), "/i.webp", stance, max)
        val striker = SkillCatalog(
            mapOf(
                1 to skill(null),
                2 to skill(null, max = false),
                3 to skill(SkillStance.Succession),
                4 to skill(SkillStance.Awakening),
                5 to Skill(setOf("wizard"), emptyMap(), "/w.webp", max = true),
                6 to Skill(setOf("striker"), emptyMap(), "/a.webp", max = true, hasPrimeVariant = true),
            ),
        )

        assertEquals(setOf(1, 3, 6), striker.choicesFor("striker", SkillStance.Succession, hideAbsoluteWithPrime = false).keys)
        assertEquals(setOf(1, 3), striker.choicesFor("striker", SkillStance.Succession, hideAbsoluteWithPrime = true).keys)
        // «Абсолют» с вариантом «Мощь» — основной навык пробуждения: там он остаётся всегда
        assertEquals(setOf(1, 4, 6), striker.choicesFor("striker", SkillStance.Awakening, hideAbsoluteWithPrime = true).keys)
        assertEquals(SkillStance.Awakening, striker.stanceOf(listOf(1, 4)))
        assertNull(striker.stanceOf(listOf(1, 2)))
    }

    @Test
    fun nameFallsBackToEnglish() {
        val skill = Skill(emptySet(), mapOf("en" to "Bolt", "ru" to "Молния"), "/i.webp")

        assertEquals("Молния", skill.name("ru"))
        assertEquals("Bolt", skill.name("de"))
    }
}
