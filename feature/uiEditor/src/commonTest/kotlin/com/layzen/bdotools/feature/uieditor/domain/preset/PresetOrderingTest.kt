package com.layzen.bdotools.feature.uieditor.domain.preset

import com.layzen.bdotools.feature.uieditor.domain.file.AnchoredLayout
import kotlin.test.Test
import kotlin.test.assertEquals

class PresetOrderingTest {

    private fun presets(vararg names: String) = names.map { NamedLayout(it, AnchoredLayout()) }

    @Test
    fun savedOrderWinsAndNewPresetsGoLastByName() {
        val sorted = presets("b", "a", "d", "c").orderedBy(listOf("c", "a", "gone"))
        assertEquals(listOf("c", "a", "b", "d"), sorted.map { it.name })
    }

    @Test
    fun movedShiftsItemAndIgnoresBadIndices() {
        assertEquals(listOf("b", "c", "a"), listOf("a", "b", "c").moved(0, 2))
        assertEquals(listOf("c", "a", "b"), listOf("a", "b", "c").moved(2, 0))
        assertEquals(listOf("a", "b", "c"), listOf("a", "b", "c").moved(1, 5))
    }
}
