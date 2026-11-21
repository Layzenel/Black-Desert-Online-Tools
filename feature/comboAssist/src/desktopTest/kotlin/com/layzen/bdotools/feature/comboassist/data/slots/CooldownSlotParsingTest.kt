package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CooldownSlotParsingTest {
    private val character = """
        <SkillCoolTimeSlot Version="2">
        <SkillCoolTimeSlot SlotNo="0" SkillNo="5338"/>
        <SkillCoolTimeSlot SlotNo="1" SkillNo="0"/>
        <SkillCoolTimeSlot SlotNo="10" SkillNo="3042"/>
        <UIData Index="128" IsShow="true"/>
        <UIData Index="148" IsShow="false"/>
    """.trimIndent()

    private val account = """
        <UiScale Value="0.8"/>
        <Resolution Width="2560" Height="1440"/>
        <UIData Index="128" IsShow="true" PendingType="LeftBottom" PosX="1250" PosY="420" RelativePosX="0.3984375" RelativePosY="0.5802332163"/>
        <UIData Index="148" IsShow="true" PendingType="RightBottom" PosX="685" PosY="370" RelativePosX="0.59375" RelativePosY="0.6079955697"/>
        <UIData Index="158" IsShow="true" RelativePosX="0.1" RelativePosY="0.7"/>
    """.trimIndent()

    @Test
    fun readsSkillsBySlotWithoutEmptyOnes() = assertEquals(mapOf(0 to 5338, 10 to 3042), parseSlotSkills(character))

    @Test
    fun slotTenIsIndex148() = assertEquals(setOf(10), parseHiddenSlots(character))

    @Test
    fun readsCentersAndScale() {
        assertEquals(mapOf(0 to (0.3984375 to 0.5802332163), 10 to (0.59375 to 0.6079955697)), parseSlotCenters(account))
        assertEquals(0.8, parseUiScale(account))
        assertEquals(1440, parseResolutionHeight(account))
    }

    @Test
    fun historyVisibilityComesFromCharacterThenAccount() {
        val hidden = """<UIData Index="158" IsShow="false"/>"""
        val shown = """<UIData Index="158" IsShow="true" PosX="1"/>"""
        assertFalse(parseHistoryShown(hidden, shown))
        assertTrue(parseHistoryShown(shown, hidden))
        assertFalse(parseHistoryShown("", hidden))
        assertTrue(parseHistoryShown("", ""))
    }
}
