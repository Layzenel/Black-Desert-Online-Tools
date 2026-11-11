package com.layzen.bdotools.feature.uieditor.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals

class SkillMarkupParsingTest {

    @Test
    fun colorsSpansAndKeys() {
        val parts = parseSkillMarkup("""<span style="color: #d8ff00">{KeyBind:Kick} </span>after Guard""" + "\n<Ammo>")

        assertEquals(
            listOf(
                SkillTextPart.Key("Kick", 0xD8FF00),
                SkillTextPart.Plain(" ", 0xD8FF00),
                SkillTextPart.Plain("after Guard\n<Ammo>"),
            ),
            parts,
        )
    }

    @Test
    fun strayClosingTagIsIgnored() {
        assertEquals(listOf(SkillTextPart.Plain("a"), SkillTextPart.Plain("b")), parseSkillMarkup("a</span>b"))
    }
}
