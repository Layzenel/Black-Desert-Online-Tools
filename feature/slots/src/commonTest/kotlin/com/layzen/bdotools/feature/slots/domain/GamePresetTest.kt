package com.layzen.bdotools.feature.slots.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Пресеты 1–3 игры: свои теги, `LeftTop` и 6 знаков в `RelativePos`. */
class GamePresetTest {

    private val presetLine =
        """	<UISettingPreset1 Index="128" IsShow="true" PendingType="LeftTop" PosX="1076" PosY="1045" RelativePosX="0.344063" RelativePosY="0.594114"/>"""
    private val text = GAME_VARIABLE_SAMPLE + presetLine + "\r\n"

    @Test
    fun readsOnlyPresetsSavedInFile() {
        val presets = parseGamePresets(text)
        assertEquals(setOf(GamePreset.General, GamePreset.Preset2), presets.keys)
        assertEquals(setOf(128), presets.getValue(GamePreset.Preset2).uiData.keys)
        assertEquals(0.344063, presets.getValue(GamePreset.Preset2).uiData.getValue(128).relativeX)
    }

    @Test
    fun writesIntoChosenPresetOnly() {
        val game = parseGameVariable(text, GamePreset.Preset2)
        val slot = SlotId(SlotGroup.Cooldown, 1)
        val moved = game.layout.withPosition(slot, requireNotNull(game.layout[slot]).movedBy(9, 0))

        val written = writeUiData(text, game.changedEntries(moved), GamePreset.Preset2)

        assertTrue(written.contains("""<UISettingPreset1 Index="128" IsShow="true" PendingType="LeftTop" PosX="1085" PosY="1045" RelativePosX="0.346875" RelativePosY="0.593889"/>"""))
        assertTrue(written.contains("""<UIData Index="128" IsShow="true" PendingType="LeftBottom" PosX="1061" """), "общий набор не тронут")
    }

    @Test
    fun marqueeTouchesFramesItCrosses() {
        val screen = ScreenSpace(2560, 1440, 0.8)
        val slot = SlotId(SlotGroup.Cooldown, 1)
        val center = SlotPosition(0, 0) // рамка 1575..1625 × 875..925
        assertTrue(ScreenArea.between(1620.0, 920.0, 1700.0, 1000.0).touches(slot, center, screen))
        assertTrue(!ScreenArea.between(1626.0, 900.0, 1700.0, 1000.0).touches(slot, center, screen))
    }

    @Test
    fun partOfLayoutKeepsOnlyChosenElements() {
        val first = SlotId(SlotGroup.Cooldown, 1)
        val second = SlotId(SlotGroup.Cooldown, 2)
        val layout = AnchoredLayout(
            positions = mapOf(first to AnchoredPosition(SlotAnchor.Crosshair, 0, 0), second to AnchoredPosition(SlotAnchor.Crosshair, 50, 0)),
            hidden = setOf(first, second),
        )
        assertEquals(AnchoredLayout(mapOf(second to AnchoredPosition(SlotAnchor.Crosshair, 50, 0)), setOf(second)), layout.only(setOf(second)))
    }
}
