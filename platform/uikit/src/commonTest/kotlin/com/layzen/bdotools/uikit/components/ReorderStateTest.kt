package com.layzen.bdotools.uikit.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ReorderStateTest {

    private fun state(vararg heights: Int): Pair<ReorderState, MutableList<Pair<Int, Int>>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        val state = ReorderState().apply {
            onMove = { from, to -> moves += from to to }
            heights.forEachIndexed { index, height -> this.heights[index] = height }
        }
        return state to moves
    }

    @Test
    fun movesDownPastHalfOfNextItems() {
        val (state, moves) = state(10, 10, 10, 10)
        state.start(0)
        state.drag(12f) // за половину второго пункта (5), но не третьего (10 + 5)
        state.end()
        assertEquals(listOf(0 to 1), moves)
    }

    @Test
    fun movesUpAndStopsAtFirst() {
        val (state, moves) = state(10, 20, 10)
        state.start(2)
        state.drag(-100f)
        state.end()
        assertEquals(listOf(2 to 0), moves)
    }

    @Test
    fun smallDragKeepsPlace() {
        val (state, moves) = state(10, 10)
        state.start(1)
        state.drag(-4f)
        state.end()
        assertEquals(emptyList(), moves)
    }

    @Test
    fun neighboursMakeRoomForDraggedItem() {
        val (state, _) = state(10, 10, 10, 10)
        state.start(0)
        state.drag(16f) // встанет на место 2: пункты 1 и 2 уступают его высоту вверх
        assertEquals(listOf(0f, -10f, -10f, 0f), (0..3).map { state.shiftOf(it) })
        state.drag(-40f) // обратно выше начала — никто не сдвигается
        assertEquals(listOf(0f, 0f, 0f, 0f), (0..3).map { state.shiftOf(it) })
    }
}
