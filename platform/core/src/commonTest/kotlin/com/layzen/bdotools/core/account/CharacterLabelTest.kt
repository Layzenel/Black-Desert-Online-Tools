package com.layzen.bdotools.core.account

import kotlin.test.Test
import kotlin.test.assertEquals

class CharacterLabelTest {

    @Test
    fun readsCaptionAndClassBadge() {
        assertEquals(CharacterLabel("Моряк", null), CharacterLabel.parse("caption=Моряк\r\nbadge=sailing\r\n"))
        assertEquals(CharacterLabel(null, "musa"), CharacterLabel.parse("badge=class-musa\n"))
        assertEquals(CharacterLabel(null, null), CharacterLabel.parse("caption=\nunknown line\n"))
    }
}
