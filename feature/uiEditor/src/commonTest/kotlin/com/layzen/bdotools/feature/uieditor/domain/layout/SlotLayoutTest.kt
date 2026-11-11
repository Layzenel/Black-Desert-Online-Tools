package com.layzen.bdotools.feature.uieditor.domain.layout

import com.layzen.bdotools.feature.uieditor.domain.preset.BuiltInLayout
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlotLayoutTest {

    private val first = SlotId(SlotGroup.Cooldown, 1)
    private val second = SlotId(SlotGroup.Cooldown, 2)
    private val layout = SlotLayout(mapOf(first to SlotPosition(-100, 50), second to SlotPosition(20, 30)))

    @Test
    fun movesOnlySelected() {
        assertEquals(SlotLayout(mapOf(first to SlotPosition(-90, 45), second to SlotPosition(20, 30))), layout.moved(setOf(first), 10, -5))
    }

    @Test
    fun overlayKeepsSlotsMissingInOther() {
        val other = SlotLayout(mapOf(second to SlotPosition(0, 0)))
        assertEquals(SlotLayout(mapOf(first to SlotPosition(-100, 50), second to SlotPosition(0, 0))), layout.overlaidWith(other))
    }

    @Test
    fun overlayTakesVisibilityOnlyForItsSlots() {
        val base = layout.withVisibility(first, false).withVisibility(second, false)
        val other = SlotLayout(mapOf(second to SlotPosition(0, 0)))
        assertEquals(setOf(first), base.overlaidWith(other).hidden)
    }

    @Test
    fun slotIndicesFollowGameNumbering() {
        assertEquals(128, SlotId(SlotGroup.Cooldown, 1).uiIndex)
        assertEquals(148, SlotId(SlotGroup.Cooldown, 11).uiIndex)
        assertEquals(100, SlotId(SlotGroup.Hotbar, 1).uiIndex)
        assertEquals(110, SlotId(SlotGroup.Hotbar, 11).uiIndex)
        assertEquals(109, SlotId(SlotGroup.Hotbar, 20).uiIndex)
    }

    @Test
    fun interfaceElementsHaveOwnFramesAndUniqueIndices() {
        val health = SlotId(SlotGroup.Interface, InterfaceElement.Health.ordinal + 1)
        assertEquals(13, health.uiIndex)
        assertEquals(385 to 40, health.width to health.height)
        assertEquals(50 to 65, SlotId(SlotGroup.Hotbar, 1).let { it.width to it.height })
        val indices = SlotGroup.entries.flatMap { it.uiIndices }
        assertEquals(indices.size, indices.toSet().size)
    }

    @Test
    fun builtInLayoutsPlaceAllCooldownSlotsApart() {
        BuiltInLayout.entries.forEach { builtIn ->
            val positions = builtIn.layout.positions
            assertEquals(SlotGroup.Cooldown.slots.toSet(), positions.keys, builtIn.name)
            assertEquals(positions.size, positions.values.toSet().size, builtIn.name)
            // Рамки 50×50 не налезают друг на друга
            val points = positions.values.toList()
            points.forEachIndexed { i, a ->
                points.drop(i + 1).forEach { b ->
                    assertTrue(abs(a.x - b.x) >= 50 || abs(a.y - b.y) >= 50, "${builtIn.name}: $a и $b")
                }
            }
        }
    }
}
