package com.layzen.bdotools.feature.uieditor.domain.file

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.preset.NamedLayout
import com.layzen.bdotools.feature.uieditor.domain.screen.SlotAnchor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LayoutCodecTest {

    private val layout = NamedLayout(
        name = "Моя раскладка",
        layout = AnchoredLayout(
            positions = mapOf(
                SlotId(SlotGroup.Cooldown, 1) to AnchoredPosition(SlotAnchor.Crosshair, -300, 270),
                SlotId(SlotGroup.Cooldown, 20) to AnchoredPosition(SlotAnchor.Crosshair, 25, 560),
                SlotId(SlotGroup.Hotbar, 11) to AnchoredPosition(SlotAnchor.BottomRight, -653, -243),
                SlotId(SlotGroup.ClassResource, 1) to AnchoredPosition(SlotAnchor.Crosshair, 0, 509),
            ),
            hidden = setOf(SlotId(SlotGroup.Cooldown, 20)),
            labels = mapOf(SlotId(SlotGroup.Cooldown, 1) to 12),
        ),
    )

    @Test
    fun labelWidthIsWrittenOnlyWhereSet() {
        assertEquals(1, Regex(""""labelChars": 12""").findAll(LayoutCodec.encode(layout)).count())
        assertEquals(0, Regex("labelChars").findAll(LayoutCodec.encode(layout.copy(layout = layout.layout.copy(labels = emptyMap())))).count())
    }

    @Test
    fun labelWidthIsClampedAndOnlyForCooldown() {
        val text = """{"format":"bdo-tools/slot-layout","version":2,"name":"a",
            |"cooldown":[{"slot":1,"x":1,"y":2,"labelChars":99},{"slot":2,"x":1,"y":2,"labelChars":0}],
            |"hotbar":[{"slot":1,"x":1,"y":2,"labelChars":5}]}""".trimMargin()
        val expected = mapOf(SlotId(SlotGroup.Cooldown, 1) to SlotLabel.MAX_CHARS, SlotId(SlotGroup.Cooldown, 2) to 1)
        assertEquals(expected, LayoutCodec.decode(text).layout.labels)
    }

    @Test
    fun roundTrip() {
        assertEquals(layout, LayoutCodec.decode(LayoutCodec.encode(layout)))
    }

    @Test
    fun skillsAndMemosRoundTripOnlyWhenIncluded() {
        val slot = SlotId(SlotGroup.Cooldown, 1)
        val withSkills = layout.copy(layout = layout.layout.copy(skills = mapOf(slot to 5338), memos = mapOf(5338 to "ЛКМ", 1 to "чужая")))

        val decoded = LayoutCodec.decode(LayoutCodec.encode(withSkills)).layout

        assertEquals(mapOf(slot to 5338), decoded.skills)
        assertEquals(mapOf(5338 to "ЛКМ"), decoded.memos) // надпись навыка не из слотов в файл не попадает
        val noMemos = withSkills.copy(layout = withSkills.layout.withSkillsIncluded(withSkills = true, withMemos = false))
        assertEquals(null, LayoutCodec.decode(LayoutCodec.encode(noMemos)).layout.memos)
        assertEquals(null, LayoutCodec.decode(LayoutCodec.encode(layout)).layout.skills)
    }

    @Test
    fun onlyKeepsMemosOfKeptSkills() {
        val slot1 = SlotId(SlotGroup.Cooldown, 1)
        val slot20 = SlotId(SlotGroup.Cooldown, 20)
        val anchored = layout.layout.copy(skills = mapOf(slot1 to 1, slot20 to 2), memos = mapOf(1 to "a", 2 to "b"))

        val part = anchored.only(setOf(slot20), emptySet())

        assertEquals(mapOf(slot20 to 2), part.skills)
        assertEquals(mapOf(2 to "b"), part.memos)
    }

    @Test
    fun writesAnchorOfEverySlot() {
        val text = LayoutCodec.encode(layout)
        assertEquals(3, Regex(""""anchor": "crosshair"""").findAll(text).count())
        assertEquals(1, Regex(""""anchor": "bottom-right"""").findAll(text).count())
    }

    @Test
    fun firstVersionCountsFromScreenCenter() {
        val text = """{"format":"bdo-tools/slot-layout","version":1,"name":"a","cooldown":[{"slot":2,"x":1,"y":2}]}"""
        val expected = mapOf(SlotId(SlotGroup.Cooldown, 2) to AnchoredPosition(SlotAnchor.ScreenCenter, 1, 2))
        assertEquals(expected, LayoutCodec.decode(text).layout.positions)
    }

    @Test
    fun slotNumbersOutOfRangeAreSkipped() {
        val text = """{"format":"bdo-tools/slot-layout","version":2,"name":"a","hotbar":[{"slot":21,"x":1,"y":2},{"slot":0,"x":1,"y":2}]}"""
        assertEquals(emptyMap(), LayoutCodec.decode(text).layout.positions)
    }

    @Test
    fun errorsAreExplained() {
        assertError(LayoutError.NotJson, "not json")
        assertError(LayoutError.NotJson, "[1, 2]")
        assertError(LayoutError.WrongFormat, """{"format":"other","version":1}""")
        assertError(LayoutError.UnsupportedVersion, """{"format":"bdo-tools/slot-layout","version":3,"name":"a"}""")
        assertError(LayoutError.NotJson, """{"format":"bdo-tools/slot-layout","version":2,"name":"a","cooldown":"x"}""")
        assertError(
            LayoutError.WrongFormat,
            """{"format":"bdo-tools/slot-layout","version":2,"name":"a","cooldown":[{"slot":1,"anchor":"left","x":1,"y":2}]}""",
        )
    }

    private fun assertError(expected: LayoutError, text: String) {
        assertEquals(expected, assertFailsWith<LayoutFormatException> { LayoutCodec.decode(text) }.error)
    }
}
