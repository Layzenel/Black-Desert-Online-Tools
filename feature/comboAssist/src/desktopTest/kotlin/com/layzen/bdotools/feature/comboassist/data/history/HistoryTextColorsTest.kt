package com.layzen.bdotools.feature.comboassist.data.history

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoryTextColorsTest {
    private fun rgb(r: Int, g: Int, b: Int) = (r shl 16) or (g shl 8) or b

    @Test
    fun whiteAndRedTextCount() {
        assertTrue(isHistoryTextPixel(rgb(242, 234, 216))) // белый текст
        assertTrue(isHistoryTextPixel(rgb(245, 150, 100))) // «Мощь:» — замер со скриншота
        assertTrue(isHistoryTextPixel(rgb(220, 140, 90)))
    }

    @Test
    fun backgroundDoesNotCount() {
        assertFalse(isHistoryTextPixel(rgb(120, 200, 90))) // светлая листва
        assertFalse(isHistoryTextPixel(rgb(150, 150, 150))) // камень
        assertFalse(isHistoryTextPixel(rgb(40, 20, 15))) // тень текста
    }
}
