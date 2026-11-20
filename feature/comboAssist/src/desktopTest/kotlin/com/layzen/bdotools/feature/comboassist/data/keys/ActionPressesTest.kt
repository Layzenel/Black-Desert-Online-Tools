package com.layzen.bdotools.feature.comboassist.data.keys

import com.layzen.bdotools.core.skill.domain.KeyBindings
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionPressesTest {
    private val keys = KeyBindings(mapOf("MoveFront" to 87, "Attack1" to 1, "Jump" to 32))

    @Test
    fun pressCarriesHeldActions() {
        val presses = ActionPresses()
        assertEquals(listOf("MoveFront" to setOf("MoveFront")), presses.onKey(RawKey(87, true), keys))
        assertEquals(listOf("Attack1" to setOf("MoveFront", "Attack1")), presses.onKey(RawKey(1, true), keys))
    }

    @Test
    fun autoRepeatIsNotNewPress() {
        val presses = ActionPresses()
        presses.onKey(RawKey(32, true), keys)
        assertEquals(emptyList(), presses.onKey(RawKey(32, true), keys))
        presses.onKey(RawKey(32, false), keys)
        assertEquals(listOf("Jump" to setOf("Jump")), presses.onKey(RawKey(32, true), keys))
    }

    @Test
    fun mouseFlagsBecomeButtons() {
        assertEquals(listOf(RawKey(1, true), RawKey(2, false)), mouseKeys(0x0001 or 0x0008))
        assertEquals(RawKey(32, false), keyboardKey(1, 32))
    }
}
