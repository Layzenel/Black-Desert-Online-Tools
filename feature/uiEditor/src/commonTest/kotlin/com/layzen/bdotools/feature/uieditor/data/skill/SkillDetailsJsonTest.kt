package com.layzen.bdotools.feature.uieditor.data.skill

import com.layzen.bdotools.feature.uieditor.domain.skill.SkillText
import kotlin.test.Test
import kotlin.test.assertEquals

class SkillDetailsJsonTest {

    @Test
    fun readsBothLanguagesWithDefaultsForMissingFields() {
        val json = """
            {"ru":{"name":"Мощь","description":"D","requiredLevel":58,"requiredMp":50,"cooldown":8000,"command":"{KeyBind:Dash}","tooltip":[["A"],["B","C"]]},
             "en":{"name":"Prime","cooldown":8000,"extra":1}}
        """.trimIndent()

        val details = parseSkillDetails(json)

        assertEquals(
            SkillText("Мощь", "D", 0, 58, 50, 0, 8000, "{KeyBind:Dash}", listOf(listOf("A"), listOf("B", "C"))),
            details.text("ru"),
        )
        assertEquals("Prime", details.text("en")?.name)
        assertEquals("Prime", details.text("de")?.name)
    }
}
