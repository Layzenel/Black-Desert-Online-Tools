package com.layzen.bdotools.feature.slots.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LayoutCodecTest {

    private val layout = NamedLayout(
        name = "Моя раскладка",
        layout = SlotLayout(
            mapOf(
                SlotId(SlotGroup.Cooldown, 1) to SlotPosition(-300, 150),
                SlotId(SlotGroup.Cooldown, 20) to SlotPosition(50, 450),
                SlotId(SlotGroup.Hotbar, 11) to SlotPosition(919, 640),
            ),
        ),
    )

    @Test
    fun roundTrip() {
        assertEquals(layout, LayoutCodec.decode(LayoutCodec.encode(layout)))
    }

    @Test
    fun missingGroupStaysMissing() {
        val text = """{"format":"bdo-tools/slot-layout","version":1,"name":"a","cooldown":[{"slot":2,"x":1,"y":2}]}"""
        assertEquals(mapOf(SlotId(SlotGroup.Cooldown, 2) to SlotPosition(1, 2)), LayoutCodec.decode(text).layout.positions)
    }

    @Test
    fun slotNumbersOutOfRangeAreSkipped() {
        val text = """{"format":"bdo-tools/slot-layout","version":1,"name":"a","hotbar":[{"slot":21,"x":1,"y":2},{"slot":0,"x":1,"y":2}]}"""
        assertEquals(emptyMap(), LayoutCodec.decode(text).layout.positions)
    }

    @Test
    fun errorsAreExplained() {
        assertError(LayoutError.NotJson, "not json")
        assertError(LayoutError.NotJson, "[1, 2]")
        assertError(LayoutError.WrongFormat, """{"format":"other","version":1}""")
        assertError(LayoutError.UnsupportedVersion, """{"format":"bdo-tools/slot-layout","version":2,"name":"a"}""")
        assertError(LayoutError.NotJson, """{"format":"bdo-tools/slot-layout","version":1,"name":"a","cooldown":"x"}""")
    }

    private fun assertError(expected: LayoutError, text: String) {
        assertEquals(expected, assertFailsWith<LayoutFormatException> { LayoutCodec.decode(text) }.error)
    }
}
