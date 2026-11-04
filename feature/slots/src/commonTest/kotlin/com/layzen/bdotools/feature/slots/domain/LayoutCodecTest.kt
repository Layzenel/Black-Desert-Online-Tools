package com.layzen.bdotools.feature.slots.domain

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
        ),
    )

    @Test
    fun roundTrip() {
        assertEquals(layout, LayoutCodec.decode(LayoutCodec.encode(layout)))
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
