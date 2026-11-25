package com.layzen.bdotools.feature.uieditor.data

import com.layzen.bdotools.feature.uieditor.data.MemorySlotsStorage.Companion.ACCOUNT
import com.layzen.bdotools.feature.uieditor.domain.game.GAME_VARIABLE_SAMPLE
import com.layzen.bdotools.feature.uieditor.domain.game.GamePreset
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.game.parseGamePresets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.coroutines.test.runTest

/** Запись в несколько наборов позиций сразу: общий и пресеты 1–3 получают одну и ту же раскладку. */
class ApplyTargetsTest {

    private val slot = SlotId(SlotGroup.Cooldown, 1)
    private val presetLines = listOf(
        """	<UISettingPreset0 Index="128" IsShow="true" PendingType="LeftTop" PosX="1076" PosY="1045" RelativePosX="0.344063" RelativePosY="0.594114"/>""",
        """	<UISettingPreset1 Index="128" IsShow="true" PendingType="LeftTop" PosX="1076" PosY="1045" RelativePosX="0.344063" RelativePosY="0.594114"/>""",
        """	<UISettingPreset0 Index="32" IsShow="true" WindowIndex="0" IsOpen="true" IsCombinedToMainPanel="true" PendingType="LeftTop" PosX="0" PosY="1417" RelativePosX="0.103437" RelativePosY="0.884231" SizeX="662" SizeY="351" FontSize="1">""",
        """		<ChatType key="Notice" value="true" colorIndexValue="-1"/>""",
    )
    private val text = GAME_VARIABLE_SAMPLE + presetLines.joinToString("\r\n", postfix = "\r\n")
    private val storage = MemorySlotsStorage(text, mutableMapOf())
    private val repository = SlotsRepositoryImpl(storage)

    @Test
    fun writesSameLayoutIntoEveryCheckedPreset() = runTest {
        val loaded = repository.load(ACCOUNT)
        val layout = loaded.game.layout.withPosition(slot, SlotPosition(100, 100))

        repository.apply(loaded, layout, setOf(GamePreset.General, GamePreset.Preset1, GamePreset.Preset2))

        val written = parseGamePresets(storage.gameVariable)
        val expected = written.getValue(GamePreset.General).layout[slot]
        assertEquals(SlotPosition(100, 100), expected)
        assertEquals(expected, written.getValue(GamePreset.Preset1).layout[slot])
        assertEquals(expected, written.getValue(GamePreset.Preset2).layout[slot])
    }

    @Test
    fun leavesUncheckedPresetsAlone() = runTest {
        val loaded = repository.load(ACCOUNT)
        val layout = loaded.game.layout.withPosition(slot, SlotPosition(100, 100))

        repository.apply(loaded, layout, setOf(GamePreset.Preset2))

        val written = parseGamePresets(storage.gameVariable)
        assertEquals(loaded.presets.getValue(GamePreset.General).layout[slot], written.getValue(GamePreset.General).layout[slot])
        assertEquals(loaded.presets.getValue(GamePreset.Preset1).layout[slot], written.getValue(GamePreset.Preset1).layout[slot])
        assertNotEquals(loaded.presets.getValue(GamePreset.Preset2).layout[slot], written.getValue(GamePreset.Preset2).layout[slot])
    }

    @Test
    fun movesChatInPreset() = runTest {
        val loaded = repository.load(ACCOUNT, GamePreset.Preset1)
        val chat = loaded.game.chats.getValue(0)
        val layout = loaded.game.layout.withChat(0, chat.copy(center = SlotPosition(chat.center.x + 50, chat.center.y)))

        repository.apply(loaded, layout, setOf(GamePreset.Preset1))

        val reread = parseGamePresets(storage.gameVariable).getValue(GamePreset.Preset1).chats.getValue(0)
        assertEquals(layout.chats.getValue(0), reread)
    }
}
