package com.layzen.bdotools.feature.slots.presentation.controller

import com.layzen.bdotools.feature.slots.domain.SlotId
import com.layzen.bdotools.feature.slots.domain.SlotLayout
import com.layzen.bdotools.feature.slots.domain.SlotPosition
import com.layzen.bdotools.feature.slots.presentation.SlotsContext
import com.layzen.bdotools.feature.slots.presentation.state.GridStep
import kotlin.math.roundToInt

/** Шаг стрелки с Shift, если сетка выключена. */
private const val NUDGE_WITHOUT_GRID = 10

/** Выбор и перемещение слотов, отмена, сетка. */
internal class EditorController(private val context: SlotsContext) {

    /** Перетаскивание: раскладка до начала, слот под курсором и накопленный сдвиг в UI-px. */
    private class DragSession(val origin: SlotLayout, val lead: SlotId) {
        var dx = 0f
        var dy = 0f
    }

    private var drag: DragSession? = null

    fun select(id: SlotId, additive: Boolean) = context.update { it.copy(selection = it.selection.toggled(id, additive)) }

    fun clearSelection() = context.update { it.copy(selection = emptySet()) }

    /** Схватить слот: без Shift выбранная группа двигается целиком, невыбранный слот выбирается один. */
    fun dragStart(id: SlotId, additive: Boolean) {
        context.update { state ->
            val selection = when {
                id in state.selection -> state.selection
                additive -> state.selection + id
                else -> setOf(id)
            }
            state.copy(selection = selection)
        }
        drag = DragSession(context.current.layout, id)
    }

    /** Сдвиг курсора с прошлого вызова. С привязкой к сетке на узел сетки встаёт слот под курсором. */
    fun dragBy(dx: Float, dy: Float) {
        val session = drag ?: return
        session.dx += dx
        session.dy += dy
        val lead = session.origin[session.lead] ?: return
        val target = SlotPosition(snapped(lead.x + session.dx), snapped(lead.y + session.dy))
        val moved = session.origin.moved(context.current.selection, target.x - lead.x, target.y - lead.y)
        context.update { it.copy(layout = moved) }
    }

    fun dragEnd() {
        val session = drag ?: return
        drag = null
        context.commitEdit(session.origin)
    }

    fun nudge(dx: Int, dy: Int, byGrid: Boolean) {
        val step = if (byGrid) context.current.grid.size.takeIf { it > 0 } ?: NUDGE_WITHOUT_GRID else 1
        val selection = context.current.selection
        context.editLayout { it.moved(selection, dx * step, dy * step) }
    }

    fun setPosition(id: SlotId, position: SlotPosition) = context.editLayout { it.withPosition(id, position) }

    fun mirror() {
        val selection = context.current.selection
        context.editLayout { it.mirrored(selection) }
    }

    fun undo() = context.update {
        val previous = it.history.lastOrNull() ?: return@update it
        it.copy(layout = previous, history = it.history.dropLast(1), activePreset = null)
    }

    fun setGrid(step: GridStep) = context.update { it.copy(grid = step) }

    fun setSnap(enabled: Boolean) = context.update { it.copy(snap = enabled) }

    private fun snapped(value: Float): Int {
        val state = context.current
        val step = state.grid.size
        return if (state.snap && step > 0) (value / step).roundToInt() * step else value.roundToInt()
    }

    private fun Set<SlotId>.toggled(id: SlotId, additive: Boolean): Set<SlotId> = when {
        !additive -> setOf(id)
        id in this -> this - id
        else -> this + id
    }
}
