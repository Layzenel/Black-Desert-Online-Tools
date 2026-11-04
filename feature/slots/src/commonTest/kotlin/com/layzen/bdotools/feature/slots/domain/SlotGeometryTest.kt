package com.layzen.bdotools.feature.slots.domain

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlotGeometryTest {

    private val screen = ScreenSpace(3200.0, 1800.0)
    private val leftBottom = UiData(128, true, "LeftBottom", 1061, 425, 0.3393749893, 0.5774569511)
    private val rightBottom = UiData(129, true, "RightBottom", 1249, 425, 0.4174999893, 0.5774569511)

    @Test
    fun positionIsFrameCenterFromScreenCenter() {
        // 0.339375 · 3200 = 1086 — левый край; центр рамки шириной 60 — 1116, от центра экрана −484
        assertEquals(SlotPosition(-484, 169), leftBottom.position(SlotGroup.Cooldown, screen))
    }

    @Test
    fun leftAnchorMovesPosXTheSameWay() {
        // Ровно этот сдвиг на 200 px мы проверили в игре: PosX 1261 → 1061
        val moved = leftBottom.movedTo(SlotPosition(-284, 169), SlotGroup.Cooldown, screen)
        assertEquals(1261, moved.posX)
        assertEquals(425, moved.posY)
        assertClose(0.401875, moved.relativeX)
    }

    @Test
    fun rightAnchorMovesPosXTheOtherWay() {
        val start = rightBottom.position(SlotGroup.Cooldown, screen)
        val moved = rightBottom.movedTo(start.movedBy(50, 0), SlotGroup.Cooldown, screen)
        assertEquals(1199, moved.posX)
    }

    @Test
    fun bottomAnchorCountsPosYUpwards() {
        val start = rightBottom.position(SlotGroup.Cooldown, screen)
        val moved = rightBottom.movedTo(start.movedBy(0, 65), SlotGroup.Cooldown, screen)
        assertEquals(360, moved.posY)
        assertEquals(start.movedBy(0, 65), moved.position(SlotGroup.Cooldown, screen))
    }

    @Test
    fun anchorStaysTheSame() {
        val moved = rightBottom.movedTo(SlotPosition(0, 0), SlotGroup.Cooldown, screen)
        assertEquals("RightBottom", moved.pendingType)
    }

    @Test
    fun onlyChangedSlotsAreRewritten() {
        val game = parseGameVariable(GAME_VARIABLE_SAMPLE)
        val first = SlotId(SlotGroup.Cooldown, 1)
        val target = game.layout.moved(setOf(first), 10, 0)
        assertEquals(listOf(128), game.changedEntries(target).map { it.index })
        assertTrue(game.changedEntries(game.layout).isEmpty())
    }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) < 1e-9, "expected $expected, got $actual")
}
