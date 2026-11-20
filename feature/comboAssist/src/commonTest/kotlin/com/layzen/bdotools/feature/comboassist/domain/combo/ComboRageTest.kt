package com.layzen.bdotools.feature.comboassist.domain.combo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ComboRageTest {
    private val normal = listOf(ComboColumn.Single(ComboCell(skillNo = 1)))
    private val rage = ComboRage(listOf(ComboColumn.Single(ComboCell(skillNo = 2))), fillers = listOf(9))
    private val combo = Combo("id", "Комбо", "seraph", normal, rage = rage)

    @Test
    fun ragePartLooksLikeCombo() {
        val part = combo.part(rageShown = true)!!
        assertEquals(rage.columns, part.columns)
        assertEquals(listOf(9), part.fillers)
        assertEquals("seraph", part.classKey)
    }

    @Test
    fun editingRagePartKeepsNormalCombo() {
        val edited = combo.part(rageShown = true)!!.copy(columns = normal)
        val changed = combo.withPart(rageShown = true, edited)
        assertEquals(normal, changed.columns)
        assertEquals(normal, changed.rage?.columns)
    }

    @Test
    fun editingNormalPartKeepsRage() {
        val edited = combo.part(rageShown = false)!!.copy(columns = emptyList())
        assertEquals(rage, combo.withPart(rageShown = false, edited).rage)
    }

    @Test
    fun noRagePartWithoutRage() {
        assertNull(combo.copy(rage = null).part(rageShown = true))
    }
}
