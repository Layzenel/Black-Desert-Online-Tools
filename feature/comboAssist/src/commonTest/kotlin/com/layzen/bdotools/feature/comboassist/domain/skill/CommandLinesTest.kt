package com.layzen.bdotools.feature.comboassist.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandLinesTest {
    private val deathstrike = "Во время навыка Алый коготь: {KeyBind:Attack2}\nПосле навыка Стальное плечо: {KeyBind:Attack2}"

    @Test
    fun lineMentioningPreviousSkillWins() {
        assertEquals("После навыка Стальное плечо: {KeyBind:Attack2}", commandLineFor(deathstrike, "Мощь: Стальное плечо IV"))
        assertEquals("Во время навыка Алый коготь: {KeyBind:Attack2}", commandLineFor(deathstrike, "<span>Мощь: Алый коготь IV</span>"))
    }

    @Test
    fun firstLineWithKeysOtherwise() {
        assertEquals("{KeyBind:Dash} + {KeyBind:Attack2}", commandLineFor("{KeyBind:Dash} + {KeyBind:Attack2}\nМожно использовать из быстрого слота", null))
        assertEquals("", commandLineFor("Пассив", null))
    }
}
