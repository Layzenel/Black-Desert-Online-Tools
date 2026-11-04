package com.layzen.bdotools.feature.slots.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameVariableParsingTest {

    private val game = parseGameVariable(GAME_VARIABLE_SAMPLE)

    @Test
    fun screenComesFromResolutionAndUiScale() {
        assertEquals(ScreenSpace(2560, 1440, 0.8), game.screen)
    }

    @Test
    fun readsOnlyEditorEntries() {
        assertEquals(setOf(128, 129, 130, 100, 89), game.uiData.keys)
        assertEquals(UiData(129, true, "RightBottom", 1249, 425, 0.4174999893, 0.5774569511), game.uiData[129])
    }

    @Test
    fun slotsFollowGroupOrder() {
        val expected = listOf(
            SlotId(SlotGroup.Cooldown, 1),
            SlotId(SlotGroup.Cooldown, 2),
            SlotId(SlotGroup.Cooldown, 3),
            SlotId(SlotGroup.Hotbar, 1),
            SlotId(SlotGroup.ClassResource, 1),
        )
        assertEquals(expected, game.slots)
    }

    @Test
    fun hiddenEntriesAreHiddenInLayout() {
        assertEquals(setOf(SlotId(SlotGroup.Cooldown, 3)), game.layout.hidden)
    }

    @Test
    fun fileWithoutSlotsIsRejected() {
        assertFailsWith<IllegalArgumentException> { parseGameVariable("""<Resolution Width="1920" Height="1080"/>""") }
    }
}
