package com.layzen.bdotools.feature.uieditor.domain.file

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.screen.SlotAnchor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Автор с 4K и масштабом 0.7 делится раскладкой с игроком на 1920×1080. */
class AnchoredLayoutTest {

    private val author = ScreenSpace(3840, 2160, 0.7)
    private val player = ScreenSpace(1920, 1080, 1.0)

    private val cooldown = SlotId(SlotGroup.Cooldown, 1)
    private val hotbarFirst = SlotId(SlotGroup.Hotbar, 1)
    private val hotbarCorner = SlotId(SlotGroup.Hotbar, 20)

    /** Слот перезарядки и хотбар 1 — у персонажа, хотбар 20 — у правого нижнего угла. */
    private val authorLayout = SlotLayout(
        mapOf(
            cooldown to SlotPosition(-314, 45),
            hotbarFirst to SlotPosition(-97, 508),
            hotbarCorner to SlotPosition(2040, 1495),
        ),
    )

    private val shared = AnchoredLayout.of(authorLayout, author)

    @Test
    fun nearestAnchorIsChosen() {
        assertEquals(SlotAnchor.Crosshair, shared.positions.getValue(cooldown).anchor)
        assertEquals(SlotAnchor.Crosshair, shared.positions.getValue(hotbarFirst).anchor)
        assertEquals(SlotAnchor.BottomRight, shared.positions.getValue(hotbarCorner).anchor)
    }

    @Test
    fun sameScreenGivesSameLayout() {
        assertEquals(authorLayout, shared.placeOn(author))
    }

    @Test
    fun slotsKeepDistanceToCrosshairAndCorner() {
        val placed = shared.placeOn(player)
        // Прицел у игрока на 79 выше центра, у автора — на 225: слот остаётся в 270 под прицелом
        assertEquals(SlotPosition(-314, 191), placed[cooldown])
        // До правого нижнего угла — те же (−703, −48)
        assertEquals(SlotPosition(257, 492), placed[hotbarCorner])
    }

    @Test
    fun smallerUiScaleIsSuggestedWhenLayoutDoesNotFit() {
        assertEquals(1, shared.offScreenCount(player))
        assertEquals(0.8, shared.suggestedUiScale(player))
    }

    @Test
    fun nothingIsSuggestedWhenEverythingFits() {
        assertNull(shared.suggestedUiScale(author))
    }
}
