package com.layzen.bdotools.core.skill.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class CrowdControlTest {
    @Test
    fun readsPvpControlsOnly() {
        val lines = listOf(
            "<PAColor0xffe9bd23>Knockdown on hits<PAOldColor>",
            "Bound on hits (PvE only)",
            "Stun Resistance +10%",
            "Floating on last hit",
            "1325% x2",
        )
        assertEquals(setOf(CrowdControl.Knockdown, CrowdControl.Floating), CrowdControl.pvpOf(lines))
    }
}
