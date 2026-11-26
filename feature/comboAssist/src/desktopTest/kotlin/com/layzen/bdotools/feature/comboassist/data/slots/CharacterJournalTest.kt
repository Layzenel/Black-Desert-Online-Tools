package com.layzen.bdotools.feature.comboassist.data.slots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CharacterJournalTest {

    @Test
    fun characterNoComesFromJournalFileName() {
        assertEquals("40000000005715195", CharacterJournal.characterNo("40000000005715195_202611.bcf"))
    }

    @Test
    fun otherFilesAreNotJournals() {
        assertNull(CharacterJournal.characterNo("WarehouseHistory_3537922.bcf"))
        assertNull(CharacterJournal.characterNo("40000000005715195_202611.bcf.tmp"))
    }
}
