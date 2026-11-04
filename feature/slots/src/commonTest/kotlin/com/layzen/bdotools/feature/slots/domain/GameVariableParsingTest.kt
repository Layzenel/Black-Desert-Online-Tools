package com.layzen.bdotools.feature.slots.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameVariableParsingTest {

    private val game = parseGameVariable(GAME_VARIABLE_SAMPLE)

    @Test
    fun screenIsResolutionDividedByUiScale() {
        assertEquals(ScreenSpace(3200.0, 1800.0), game.screen)
    }

    @Test
    fun readsOnlySlotEntries() {
        assertEquals(setOf(128, 129, 130, 100), game.uiData.keys)
        assertEquals(UiData(129, true, "RightBottom", 1249, 425, 0.4174999893, 0.5774569511), game.uiData[129])
    }

    @Test
    fun hiddenSlotsAreNotVisible() {
        val visible = game.visibleSlots
        assertEquals(listOf(SlotId(SlotGroup.Cooldown, 1), SlotId(SlotGroup.Cooldown, 2), SlotId(SlotGroup.Hotbar, 1)), visible)
    }

    @Test
    fun fileWithoutSlotsIsRejected() {
        assertFailsWith<IllegalArgumentException> { parseGameVariable("""<Resolution Width="1920" Height="1080"/>""") }
    }
}
