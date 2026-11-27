package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PresetSelectionTest {
    private fun line(time: String, preset: Int) =
        """{"Date":"2026-11-27 $time","Thread":"ucrtbase.dll(0x5BBC)","LogType":"(00/0/1)","ErrNo":"eErrNoServiceLog","ErrStr":"x","Log":"*** save UISetting PresetInfo preset No = $preset" }"""

    private fun PresetSelection.load(time: String) = (0..3).forEach { feed(line(time, it)); feed(line(time, it)) }

    @Test
    fun loadIsNotSelection() {
        val selection = PresetSelection()
        selection.load("00:02:26")
        assertNull(selection.selected)
    }

    @Test
    fun singlePresetIsSelection() {
        val selection = PresetSelection()
        selection.load("00:02:26")
        selection.feed(line("13:18:00", 1))
        selection.feed(line("13:18:00", 1))
        selection.feed("""{"Date":"2026-11-27 13:18:00","Log":"prev UIMODE:32 current UIMODE:0" }""")
        assertEquals(1, selection.selected?.second)
    }

    @Test
    fun loadKeepsEarlierSelectionWithItsTime() {
        val selection = PresetSelection()
        selection.feed(line("13:18:00", 2))
        val chosen = selection.selected
        selection.load("13:20:07")
        assertEquals(chosen, selection.selected)
        selection.feed(line("13:25:00", 0))
        assertEquals(0, selection.selected?.second)
    }
}
