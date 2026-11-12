package com.layzen.bdotools.feature.uieditor.domain.skill

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandActionsTest {

    private val green = 0xD8FF00

    @Test
    fun keepsOnlyGreenPartsOfEachLine() {
        val command = "<span style=\"color: #d8ff00\">{KeyBind:MoveBack} + {KeyBind:Kick}</span>\n" +
            "Для заряда: удерживать <span style=\"color: #d8ff00\">{KeyBind:Kick}</span>\n" +
            "Возможно использование из быстрого слота"
        assertEquals(
            listOf(
                listOf(SkillTextPart.Key("MoveBack", green), SkillTextPart.Plain(" + ", green), SkillTextPart.Key("Kick", green)),
                listOf(SkillTextPart.Key("Kick", green)),
            ),
            commandActions(command),
        )
    }

    @Test
    fun otherColorsAreNotKeys() {
        assertEquals(emptyList(), commandActions("Скорость <span style=\"color: #f8c038\">+30%</span>\n"))
        assertEquals(1, commandActions("<span style=\"color: #d8ff00\">{KeyBind:Kick}</span>\n<span style=\"color: #d8ff00\">{KeyBind:Kick}</span>").size)
    }
}
