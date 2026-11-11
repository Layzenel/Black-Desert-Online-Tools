package com.layzen.bdotools.feature.uieditor.domain.layout

import com.layzen.bdotools.feature.uieditor.domain.skill.CharacterSkills
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SlotLayoutSkillsTest {

    private val slot1 = SlotId(SlotGroup.Cooldown, 1)
    private val slot2 = SlotId(SlotGroup.Cooldown, 2)
    private val character = CharacterSkills("1", slots = mapOf(0 to 100, 1 to 200), memos = mapOf(100 to "A", 999 to "other"))

    @Test
    fun characterSkillsGoToCooldownSlotsByNumber() {
        val layout = SlotLayout().withCharacterSkills(character)

        assertEquals(mapOf(slot1 to 100, slot2 to 200), layout.skills)
        assertEquals("A", layout.memoAt(slot1))
        assertNull(layout.memoAt(slot2))
        assertFalse(layout.skillsDifferFrom(character))
    }

    @Test
    fun editsChangeSkillsAndMemos() {
        val layout = SlotLayout().withCharacterSkills(character)
            .withSkill(slot2, null)
            .withSkill(SlotId(SlotGroup.Hotbar, 1), 5)
            .withMemo(100, " ")

        assertEquals(mapOf(slot1 to 100), layout.skills)
        assertNull(layout.memoAt(slot1))
        assertTrue(layout.skillsDifferFrom(character))
    }

    @Test
    fun layoutWithoutSkillsDoesNotDifferAndKeepsSkillsWhenReplaced() {
        val withSkills = SlotLayout().withCharacterSkills(character)
        val fromGameFile = SlotLayout(positions = mapOf(slot1 to SlotPosition(1, 1)))

        assertFalse(fromGameFile.skillsDifferFrom(character))
        assertEquals(withSkills.skills, withSkills.withSkillsOf(fromGameFile).skills)
        assertEquals(fromGameFile.positions, withSkills.withSkillsOf(fromGameFile).positions)
    }

    @Test
    fun skillMovesToEmptySlotOrSwapsWithMemos() {
        val layout = SlotLayout().withCharacterSkills(character)
        val slot3 = SlotId(SlotGroup.Cooldown, 3)

        val moved = layout.withSkillMoved(slot1, slot3)
        assertEquals(mapOf(slot2 to 200, slot3 to 100), moved.skills)
        assertEquals("A", moved.memoAt(slot3))

        val swapped = layout.withSkillMoved(slot1, slot2)
        assertEquals(mapOf(slot1 to 200, slot2 to 100), swapped.skills)
        assertEquals("A", swapped.memoAt(slot2))

        assertEquals(layout, layout.withSkillMoved(slot1, SlotId(SlotGroup.Hotbar, 1)))
        assertEquals(layout, layout.withSkillMoved(slot3, slot1))
    }

    @Test
    fun overlayWithoutSkillsKeepsThem() {
        val layout = SlotLayout().withCharacterSkills(character)
        val positionsOnly = SlotLayout(positions = mapOf(slot1 to SlotPosition(0, 0)))

        assertEquals(layout.skills, layout.overlaidWith(positionsOnly).skills)
        assertEquals(layout.memos, layout.overlaidWith(positionsOnly).memos)
    }

    @Test
    fun overlayWithSkillsReplacesOnlyItsSlots() {
        val layout = SlotLayout().withCharacterSkills(character)
        val imported = SlotLayout(
            positions = mapOf(slot1 to SlotPosition(0, 0)),
            skills = emptyMap(),
            memos = mapOf(200 to "B"),
        )

        val overlaid = layout.overlaidWith(imported)

        assertEquals(mapOf(slot2 to 200), overlaid.skills)
        assertEquals("B", overlaid.memoAt(slot2))
    }
}
