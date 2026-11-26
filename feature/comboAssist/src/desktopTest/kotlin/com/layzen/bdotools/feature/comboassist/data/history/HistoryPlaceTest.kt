package com.layzen.bdotools.feature.comboassist.data.history

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HistoryPlaceTest {

    @Test
    fun readsHistoryCenterAndScale() {
        val account = """
            <UiScale Value="0.8"/>
            <Resolution Width="2560" Height="1440"/>
            <UIData Index="158" IsShow="true" PendingType="LeftBottom" PosX="18" PosY="142" RelativePosX="0.03687499836" RelativePosY="0.7429205775"/>
        """.trimIndent()
        assertEquals(HistoryPlace(0.03687499836, 0.7429205775, 0.8, 1440), HistoryPlace.parse(account))
    }

    @Test
    fun withoutPositionThereIsNoPlace() {
        assertNull(HistoryPlace.parse("""<Resolution Width="2560" Height="1440"/><UIData Index="158" IsShow="true"/>"""))
    }
}
