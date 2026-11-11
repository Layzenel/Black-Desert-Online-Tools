package com.layzen.bdotools.feature.uieditor.domain.game

import com.layzen.bdotools.feature.uieditor.domain.file.AnchoredLayout
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutCodec
import com.layzen.bdotools.feature.uieditor.domain.layout.InterfaceElement
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SplitHpTest {

    private val combined = GAME_VARIABLE_SAMPLE +
        "\r\n<UIValue Version=\"4\">\r\n\t<Bool Index=\"1\" Value=\"true\" StorageType=\"1\"/>\r\n\t<Bool Index=\"72\" Value=\"true\" StorageType=\"1\"/>\r\n</UIValue>"

    private val hpElements = listOf(InterfaceElement.Health, InterfaceElement.Pvp, InterfaceElement.BlackSpiritRage).map { it.slotId }

    @Test
    fun readsInvertedFlag() {
        assertEquals(false, parseGameVariable(combined).splitHp)
        assertEquals(true, parseGameVariable(combined.replace("""Index="72" Value="true"""", """Index="72" Value="false"""")).splitHp)
        assertNull(parseGameVariable(GAME_VARIABLE_SAMPLE).splitHp)
    }

    @Test
    fun writesOnlyFlagValue() {
        val written = writeSplitHp(combined, enabled = true)
        assertEquals(combined.replace("""Index="72" Value="true"""", """Index="72" Value="false""""), written)
        assertEquals(true, parseGameVariable(written).splitHp)
        assertEquals(true, parseGameVariable(written).customQuickSlots)
    }

    @Test
    fun insertsMissingFlagIntoUiValue() {
        val withoutFlag = combined.replace("\r\n\t<Bool Index=\"72\" Value=\"true\" StorageType=\"1\"/>", "")
        val written = writeSplitHp(withoutFlag, enabled = false)
        assertEquals(false, parseGameVariable(written).splitHp)
        assertEquals(written.count { it == '\n' }, written.count { it == '\r' })
    }

    @Test
    fun toggleHidesHpElementsLikeTheGame() {
        val off = SlotLayout().withSplitHp(false)
        assertTrue(hpElements.none { off.isVisible(it) || off.isActive(it) })
        assertTrue(off.isActive(InterfaceElement.Buffs.slotId))

        val on = off.withSplitHp(true)
        assertTrue(hpElements.all { on.isVisible(it) && on.isActive(it) })
    }

    @Test
    fun flagTravelsThroughLayoutFile() {
        val decoded = LayoutCodec.decode(LayoutCodec.encode(NamedLayout("a", AnchoredLayout(splitHp = false))))
        assertEquals(false, decoded.layout.splitHp)
        assertFalse("splitHp" in LayoutCodec.encode(NamedLayout("a", AnchoredLayout())))
        assertEquals(false, AnchoredLayout(splitHp = false).only(emptySet(), emptySet()).splitHp)
    }

    @Test
    fun importWithoutFlagKeepsCurrent() {
        assertEquals(false, SlotLayout(splitHp = false).overlaidWith(SlotLayout()).splitHp)
    }

    @Test
    fun changeIsDetected() {
        val game = parseGameVariable(combined)
        assertTrue(game.splitHpChanged(game.layout.withSplitHp(true)))
        assertFalse(game.splitHpChanged(game.layout))
    }
}
