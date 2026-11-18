package com.layzen.bdotools.core.skill.data

import com.layzen.bdotools.core.skill.domain.Skill
import kotlin.test.Test
import kotlin.test.assertEquals

class SkillCatalogJsonTest {

    @Test
    fun readsSkillsJsonFromDataSite() {
        val json = """
            {
            "5338":{"classes":["striker"],"tree":"succession","en":"Prime: Knee Hammer III","ru":"Мощь: Коленный молот III","icon":"/assets/a.webp"},
            "x":{"classes":[],"tree":"succession","en":"Bad","ru":"Плохой","icon":"/b.webp"}
            }
        """.trimIndent()

        val catalog = parseSkillCatalog(json)

        assertEquals(setOf(5338), catalog.skills.keys)
        assertEquals(
            Skill(setOf("striker"), mapOf("en" to "Prime: Knee Hammer III", "ru" to "Мощь: Коленный молот III"), "/assets/a.webp"),
            catalog[5338],
        )
    }
}
