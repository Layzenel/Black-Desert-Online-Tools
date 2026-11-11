package com.layzen.bdotools.feature.uieditor.domain.arrangement

import com.layzen.bdotools.feature.uieditor.domain.layout.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotGroup
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GroupSpreadingTest {

    // Две колонки по два слота (50×50, вплотную по вертикали) слева и справа от центра
    private val leftTop = SlotId(SlotGroup.Cooldown, 1)
    private val leftBottom = SlotId(SlotGroup.Cooldown, 2)
    private val rightTop = SlotId(SlotGroup.Cooldown, 3)
    private val rightBottom = SlotId(SlotGroup.Cooldown, 4)
    private val layout = SlotLayout(
        mapOf(
            leftTop to SlotPosition(-200, 0),
            leftBottom to SlotPosition(-200, 50),
            rightTop to SlotPosition(200, 0),
            rightBottom to SlotPosition(200, 50),
        ),
    )
    private val columns = setOf(leftTop, leftBottom, rightTop, rightBottom)

    @Test
    fun columnsAreTwoGroupsSpreadHorizontally() {
        // Плотнее 28: (50 + 4 + 1) / 400 от отдаления 200 — колонки сошлись бы вплотную
        assertEquals(SpreadAxis(value = 200, min = 28), layout.groupSpread(columns, emptySet(), horizontal = true))
        assertNull(layout.groupSpread(columns, emptySet(), horizontal = false))
    }

    @Test
    fun spreadMovesColumnsSymmetricallyAndKeepsThemWhole() {
        val result = layout.withGroupSpread(columns, emptySet(), horizontal = true, value = 300)
        assertEquals(listOf(-300, -300, 300, 300), listOf(leftTop, leftBottom, rightTop, rightBottom).map { result[it]!!.x })
        assertEquals(listOf(0, 50, 0, 50), listOf(leftTop, leftBottom, rightTop, rightBottom).map { result[it]!!.y })
    }

    @Test
    fun spreadStopsBeforeGroupsMerge() {
        val result = layout.withGroupSpread(columns, emptySet(), horizontal = true, value = 0)
        assertEquals(-28, result[leftTop]!!.x)
        assertEquals(28, result[rightTop]!!.x)
    }

    @Test
    fun selectedChatIsAGroupAndMovesWithSpread() {
        val slot = SlotId(SlotGroup.Cooldown, 1)
        val withChat = SlotLayout(
            positions = mapOf(slot to SlotPosition(0, -300)),
            chats = mapOf(0 to ChatWindow(SlotPosition(0, 300), width = 400, height = 200)),
        )
        // Центры по вертикали -300 и 300, центр между ними — 0
        assertEquals(300, withChat.groupSpread(setOf(slot), setOf(0), horizontal = false)?.value)
        val result = withChat.withGroupSpread(setOf(slot), setOf(0), horizontal = false, value = 400)
        assertEquals(SlotPosition(0, -400), result[slot])
        assertEquals(SlotPosition(0, 400), result.chats.getValue(0).center)
    }

    @Test
    fun movedWithChatsSkipsTabsOfMainWindow() {
        val tab = ChatWindow(SlotPosition(0, 0), width = 300, height = 200, combined = true)
        val moved = SlotLayout(chats = mapOf(1 to tab)).moved(emptySet(), setOf(1), 10, 10)
        assertEquals(tab, moved.chats.getValue(1))
    }
}
