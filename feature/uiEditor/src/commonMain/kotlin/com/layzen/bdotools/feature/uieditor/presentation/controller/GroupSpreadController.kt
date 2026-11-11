package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.arrangement.withGroupSpread
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext

/**
 * Слайдеры «Отдаление» у нескольких выбранных групп: каждый шаг считается от раскладки на начало жеста,
 * поэтому округление не копится, а группы не сливаются посреди жеста. Жест уходит в «Отменить» одним шагом.
 */
internal class GroupSpreadController(private val context: SlotsContext) {

    /** Раскладка до начала жеста слайдера. */
    private var origin: SlotLayout? = null

    /** Отодвигает группы выбранных элементов друг от друга на [value]; весь жест слайдера считается от раскладки до его начала. */
    fun spread(horizontal: Boolean, value: Int) {
        val state = context.current
        val start = origin ?: state.layout.also { origin = it }
        context.update { it.copy(layout = start.withGroupSpread(state.selection, state.selectedChats, horizontal, value)) }
    }

    /** Конец жеста слайдера: заносит результат в историю «Отменить». */
    fun end() {
        val previous = origin ?: return
        origin = null
        context.commitEdit(previous)
    }
}
