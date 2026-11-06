package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.withGroupSpread
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext

/**
 * Слайдеры «Отдаление» у нескольких выбранных групп: каждый шаг считается от раскладки на начало жеста,
 * поэтому округление не копится, а группы не сливаются посреди жеста. Жест уходит в «Отменить» одним шагом.
 */
internal class GroupSpreadController(private val context: SlotsContext) {

    /** Раскладка до начала жеста слайдера. */
    private var origin: SlotLayout? = null

    fun spread(horizontal: Boolean, value: Int) {
        val state = context.current
        val start = origin ?: state.layout.also { origin = it }
        context.update { it.copy(layout = start.withGroupSpread(state.selection, state.selectedChats, horizontal, value)) }
    }

    fun end() {
        val previous = origin ?: return
        origin = null
        context.commitEdit(previous)
    }
}
