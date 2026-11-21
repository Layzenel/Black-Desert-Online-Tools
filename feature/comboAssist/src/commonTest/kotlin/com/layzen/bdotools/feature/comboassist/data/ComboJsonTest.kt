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
    fun keepsPool() {
        val combo = Combo("id", "", "seraph", listOf(ComboColumn.Single(ComboCell(skillNo = 1))), pool = listOf(5, 6))
        assertEquals(listOf(combo), parseCombos(combosJson(listOf(combo))))
    }

    @Test
    fun keepsKeyTracked() {
        val combo = Combo("id", "", "striker", listOf(ComboColumn.Single(ComboCell(skillNo = 1))), keyTracked = setOf(3, 2))
        assertEquals(listOf(combo), parseCombos(combosJson(listOf(combo))))
    }

    @Test
    fun keepsCellKeys() {
        // одно умение в двух шагах — разными командами
        val columns = listOf(ComboColumn.Single(ComboCell(1, keys = setOf("Attack1"))), ComboColumn.Single(ComboCell(1, keys = setOf("Dash", "Jump"))))
        val combo = Combo("id", "", "tosa", columns)
        assertEquals(listOf(combo), parseCombos(combosJson(listOf(combo))))
    }

    @Test
    fun oldOptionalCellsMoveToPool() {
        // до пула были опциональные ячейки: одиночная колонка пропадает, из развилки ячейка уходит
        val text = """{"combos":[{"id":"id","name":"","classKey":"striker","columns":[
            {"cell":{"skill":1}},
            {"cell":{"skill":2,"optional":true}},
            {"lanes":[[{"skill":3}],[{"skill":4,"optional":true}]]}
        ]}]}"""
        val combo = parseCombos(text).single()
        assertEquals(listOf(ComboColumn.Single(ComboCell(1)), ComboColumn.Single(ComboCell(3))), combo.columns)
        assertEquals(listOf(2, 4), combo.pool)
    }

    @Test
    fun removedSchemaStyleFallsBackToNext() {
        val text = """{"combos":[{"id":"id","name":"","classKey":"striker","overlayStyle":"Schema"}]}"""
        assertEquals(OverlayStyle.Next, parseCombos(text).single().overlayStyle)
    }
}
