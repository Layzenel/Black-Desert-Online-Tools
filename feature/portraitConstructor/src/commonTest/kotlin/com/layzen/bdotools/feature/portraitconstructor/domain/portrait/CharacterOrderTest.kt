package com.layzen.bdotools.feature.portraitconstructor.domain.portrait

import kotlin.test.Test
import kotlin.test.assertEquals

class CharacterOrderTest {

    @Test
    fun sortsByOrderAndPutsUnknownAtTheEnd() {
        val order = mapOf("40000000003247543" to 0, "40000000003249407" to 2, "40000000008825214" to 1)
        val files = listOf("40000000003249407", "40000000009999999", "40000000003247543", "40000000008825214", "40000000001111111")

        assertEquals(
            listOf("40000000003247543", "40000000008825214", "40000000003249407", "40000000001111111", "40000000009999999"),
            sortByCharacterOrder(files, order),
        )
    }
}
