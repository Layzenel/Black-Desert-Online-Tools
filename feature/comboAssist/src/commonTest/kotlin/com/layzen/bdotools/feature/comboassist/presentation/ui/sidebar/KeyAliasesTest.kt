package com.layzen.bdotools.feature.comboassist.presentation.ui.sidebar

import kotlin.test.Test
import kotlin.test.assertEquals

class KeyAliasesTest {
    @Test
    fun parsesLabelsAndSpellings() {
        assertEquals(mapOf("лкм" to listOf("lmb"), "shift" to listOf("шифт", "шифтом")), keyAliases("ЛКМ=lmb; Shift=шифт,шифтом"))
        assertEquals(emptyMap(), keyAliases(""))
    }
}
