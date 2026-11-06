package com.layzen.bdotools.feature.portraitconstructor.presentation.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReorderStateTest {

    @Test
    fun secondDifferentPortraitMakesPair() {
        val picked = ReorderState().toggled("1")
        assertNull(ReorderState().pairWith("1"))
        assertEquals("1" to "2", picked.pairWith("2"))
    }

    @Test
    fun clickOnFirstDeselectsIt() {
        val picked = ReorderState().toggled("1")
        assertNull(picked.pairWith("1"))
        assertNull(picked.toggled("1").first)
    }
}
