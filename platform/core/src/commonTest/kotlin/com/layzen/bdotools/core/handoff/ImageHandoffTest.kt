package com.layzen.bdotools.core.handoff

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageHandoffTest {
    @Test
    fun sentArrivesEvenIfReceiverTookPictureAtOnce() = runTest {
        val handoff = ImageHandoff()
        val sent = async(UnconfinedTestDispatcher(testScheduler)) { handoff.sent.first() }
        handoff.send(HandoffTarget.PortraitBackground, byteArrayOf(1))
        assertContentEquals(byteArrayOf(1), handoff.take(HandoffTarget.PortraitBackground))
        assertNull(handoff.pending.value)
        assertEquals(HandoffTarget.PortraitBackground, sent.await())
    }
}
