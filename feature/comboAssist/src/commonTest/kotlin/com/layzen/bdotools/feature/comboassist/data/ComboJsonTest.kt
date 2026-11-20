package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.feature.comboassist.domain.combo.Combo
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboCell
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboColumn
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboRage
import com.layzen.bdotools.feature.comboassist.domain.combo.OverlayStyle
import com.layzen.bdotools.feature.comboassist.domain.settings.OverlaySettings
import kotlin.test.Test
import kotlin.test.assertEquals

class ComboJsonTest {

    @Test
    fun keepsCellNoteAndSchemaSetting() {
        val combo = Combo("id", "Комбо", "striker", listOf(ComboColumn.Single(ComboCell(skillNo = 1, note = "в прыжке"))))
        val settings = OverlaySettings(schemaShown = true, slotShields = true)
        val text = combosJson(listOf(combo), settings)
        assertEquals(listOf(combo), parseCombos(text))
        assertEquals(settings, parseSettings(text))
    }

    @Test
    fun keepsRageCombo() {
        val rage = ComboRage(listOf(ComboColumn.Single(ComboCell(skillNo = 2, chain = true))), fillers = listOf(3))
        val combo = Combo("id", "", "seraph", listOf(ComboColumn.Single(ComboCell(skillNo = 1))), rage = rage)
        assertEquals(listOf(combo), parseCombos(combosJson(listOf(combo))))
    }

    @Test
    fun removedSchemaStyleFallsBackToNext() {
        val text = """{"combos":[{"id":"id","name":"","classKey":"striker","overlayStyle":"Schema"}]}"""
        assertEquals(OverlayStyle.Next, parseCombos(text).single().overlayStyle)
    }
}
