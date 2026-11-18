package com.layzen.bdotools.feature.gameinfo.domain.ping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PingQualityTest {
    @Test
    fun bordersBelongToTheWorseQuality() {
        assertEquals(PingQuality.Good, pingQuality(59))
        assertEquals(PingQuality.Fair, pingQuality(60))
        assertEquals(PingQuality.Poor, pingQuality(120))
        assertEquals(PingQuality.Bad, pingQuality(200))
    }

    @Test
    fun unknownPingHasNoQuality() = assertNull(pingQuality(null))
}
