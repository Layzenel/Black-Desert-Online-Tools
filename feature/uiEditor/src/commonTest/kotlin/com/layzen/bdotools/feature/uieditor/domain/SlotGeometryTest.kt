package com.layzen.bdotools.feature.uieditor.domain

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlotGeometryTest {

    private val screen = ScreenSpace(2560, 1440, 0.8)
    private val leftBottom = UiData(128, true, "LeftBottom", 1061, 425, 0.3393749893, 0.5774569511)
    private val rightBottom = UiData(129, true, "RightBottom", 1249, 425, 0.4174999893, 0.5774569511)

    @Test
    fun screenIsResolutionDividedByUiScale() {
        assertEquals(3200.0, screen.width)
        assertEquals(1800.0, screen.height)
    }

    @Test
    fun crosshairIsAboveCenter() {
        // По скриншоту 2560×1440 при масштабе 0.8: прицел в 131 UI-px над центром
        assertEquals(SlotPosition(0, -131), screen.crosshair)
    }

    @Test
    fun relativePositionIsIconCenter() {
        // 0.339375 · 3200 = 1086 — центр иконки; от центра экрана −514
        assertEquals(SlotPosition(-514, 139), leftBottom.position(screen))
    }

    @Test
    fun leftAnchorMovesPosXTheSameWay() {
        // Ровно этот сдвиг на 200 px мы проверили в игре: PosX 1061 → 1261
        val moved = leftBottom.movedTo(SlotPosition(-314, 139), screen)
        assertEquals(1261, moved.posX)
        assertEquals(425, moved.posY)
        assertClose(0.401875, moved.relativeX)
    }

    @Test
    fun rightAnchorMovesPosXTheOtherWay() {
        val start = rightBottom.position(screen)
        val moved = rightBottom.movedTo(start.movedBy(50, 0), screen)
        assertEquals(1199, moved.posX)
    }

    @Test
    fun bottomAnchorCountsPosYUpwards() {
        val start = rightBottom.position(screen)
        val moved = rightBottom.movedTo(start.movedBy(0, 65), screen)
        assertEquals(360, moved.posY)
        assertEquals(start.movedBy(0, 65), moved.position(screen))
    }

    @Test
    fun anchorStaysTheSame() {
        assertEquals("RightBottom", rightBottom.movedTo(SlotPosition(0, 0), screen).pendingType)
    }

    @Test
    fun onlyChangedSlotsAreRewritten() {
        val game = parseGameVariable(GAME_VARIABLE_SAMPLE)
        val first = SlotId(SlotGroup.Cooldown, 1)
        assertEquals(listOf(128), game.changedEntries(game.layout.moved(setOf(first), 10, 0)).map { it.index })
        assertTrue(game.changedEntries(game.layout).isEmpty())
    }

    @Test
    fun visibilityChangeIsRewrittenWithoutMoving() {
        val game = parseGameVariable(GAME_VARIABLE_SAMPLE)
        val third = SlotId(SlotGroup.Cooldown, 3)
        val changed = game.changedEntries(game.layout.withVisibility(third, true)).single()
        assertEquals(game.uiData.getValue(130).copy(isShow = true), changed)
    }

    @Test
    fun iconFitsOnlyInsideScreen() {
        val slot = SlotId(SlotGroup.Cooldown, 1)
        assertTrue(screen.fits(slot, SlotPosition(1600 - 25, 0)))
        assertTrue(!screen.fits(slot, SlotPosition(1600 - 24, 0)))
    }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) < 1e-9, "expected $expected, got $actual")
}
