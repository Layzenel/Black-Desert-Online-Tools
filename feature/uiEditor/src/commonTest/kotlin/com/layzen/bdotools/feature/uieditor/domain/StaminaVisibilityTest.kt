package com.layzen.bdotools.feature.uieditor.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StaminaVisibilityTest {

    private val stamina = InterfaceElement.Stamina.slotId

    /** Выносливость в файле скрыта: игра спрятала её, пока выносливость не расходуется. */
    private val text = GAME_VARIABLE_SAMPLE +
        """	<UIData Index="122" IsShow="false" PendingType="RightBottom" PosX="1082" PosY="354" RelativePosX="0.5" RelativePosY="0.6077179313"/>""" + "\r\n"

    @Test
    fun staminaIsAlwaysVisibleAndHasNoToggle() {
        val layout = parseGameVariable(text).layout
        assertFalse(stamina.hasVisibilityToggle)
        assertTrue(layout.isVisible(stamina))
        assertEquals(layout, layout.withVisibility(stamina, false))
    }

    @Test
    fun movedStaminaKeepsIsShowFromFile() {
        val game = parseGameVariable(text)
        val position = game.layout.getValue(stamina).movedBy(10, 0)
        val entry = game.changedEntries(game.layout.withPosition(stamina, position)).single()
        assertFalse(entry.isShow)
    }

    private fun SlotLayout.getValue(id: SlotId) = requireNotNull(this[id])
}
