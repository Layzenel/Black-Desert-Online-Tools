package com.layzen.bdotools.feature.portraitconstructor.domain.memo

import kotlin.test.Test
import kotlin.test.assertEquals

class MemoChangesTest {

    @Test
    fun changesSkipEmptyAndMatchingCaptions() {
        val memos = mapOf("1" to "Коневод", "2" to "Золотой свин", "3" to "Чтец")
        val captions = listOf("1" to "Коневод", "2" to "Свин", "3" to "", "4" to " Рыбак ")

        assertEquals(
            listOf(MemoChange("2", "Золотой свин", "Свин"), MemoChange("4", "", "Рыбак")),
            memoChanges(captions, memos),
        )
    }
}
