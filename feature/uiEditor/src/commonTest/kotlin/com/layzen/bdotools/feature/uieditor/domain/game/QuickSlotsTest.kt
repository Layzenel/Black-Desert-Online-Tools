package com.layzen.bdotools.feature.uieditor.domain.game

import com.layzen.bdotools.feature.uieditor.domain.file.AnchoredLayout
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutCodec
import com.layzen.bdotools.feature.uieditor.domain.layout.InterfaceElement
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuickSlotsTest {

    private val withFlag = GAME_VARIABLE_SAMPLE +
        "\r\n<UIValue Version=\"4\">\r\n\t<Bool Index=\"17\" Value=\"true\" StorageType=\"1\"/>\r\n\t<Bool Index=\"1\" Value=\"true\" StorageType=\"1\"/>\r\n</UIValue>"

    @Test
    fun readsFlag() {
        assertEquals(true, parseGameVariable(withFlag).customQuickSlots)
        assertNull(parseGameVariable(GAME_VARIABLE_SAMPLE).customQuickSlots)
    }

    @Test
    fun writesOnlyFlagValue() {
        val written = writeCustomQuickSlots(withFlag, enabled = false)
        assertEquals(withFlag.replace("""Index="1" Value="true"""", """Index="1" Value="false""""), written)
        assertEquals(false, parseGameVariable(written).customQuickSlots)
    }

    @Test
    fun insertsMissingFlagIntoUiValue() {
        val withoutFlag = withFlag.replace("\r\n\t<Bool Index=\"1\" Value=\"true\" StorageType=\"1\"/>", "")
        val written = writeCustomQuickSlots(withoutFlag, enabled = false)
        assertEquals(false, parseGameVariable(written).customQuickSlots)
        assertEquals(withoutFlag.count { it == '\n' } + 1, written.count { it == '\n' })
        assertEquals(written.count { it == '\n' }, written.count { it == '\r' })
    }

    @Test
    fun toggleSwapsHotbarAndQuickSlotLikeTheGame() {
        val quickSlot = InterfaceElement.QuickSlot.slotId
        val off = SlotLayout(hidden = setOf(quickSlot)).withCustomQuickSlots(false)
        assertTrue(off.isVisible(quickSlot))
        assertTrue(SlotGroup.Hotbar.slots.none { off.isVisible(it) })
        assertFalse(SlotGroup.Hotbar.slots.first().isActiveWith(off.customQuickSlots, null))

        val on = off.withCustomQuickSlots(true)
        assertFalse(on.isVisible(quickSlot))
        assertTrue(SlotGroup.Hotbar.slots.all { on.isVisible(it) })
        assertFalse(quickSlot.isActiveWith(on.customQuickSlots, null))
    }

    @Test
    fun flagTravelsThroughLayoutFile() {
        val decoded = LayoutCodec.decode(LayoutCodec.encode(NamedLayout("a", AnchoredLayout(customQuickSlots = false))))
        assertEquals(false, decoded.layout.customQuickSlots)
        assertFalse("customQuickSlots" in LayoutCodec.encode(NamedLayout("a", AnchoredLayout())))
    }

    @Test
    fun importWithoutFlagKeepsCurrent() {
        assertEquals(false, SlotLayout(customQuickSlots = false).overlaidWith(SlotLayout()).customQuickSlots)
    }
}
