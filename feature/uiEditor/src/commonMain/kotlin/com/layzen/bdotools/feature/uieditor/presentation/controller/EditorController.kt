package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.AlignEdge
import com.layzen.bdotools.feature.uieditor.domain.ScreenArea
import com.layzen.bdotools.feature.uieditor.domain.SlotId
import com.layzen.bdotools.feature.uieditor.domain.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.SnapGuides
import com.layzen.bdotools.feature.uieditor.domain.aligned
import com.layzen.bdotools.feature.uieditor.domain.distributed
import com.layzen.bdotools.feature.uieditor.domain.snapLines
import com.layzen.bdotools.feature.uieditor.domain.snapToLines
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.GridStep
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsState
import kotlin.math.roundToInt

/** Шаг стрелки с Shift, если привязка к сетке выключена. */
private const val NUDGE_WITHOUT_SNAP = 10

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

    /** Выбирает всё, что задела обводка; с [additive] — вместе с уже выбранным. */
    fun selectArea(area: ScreenArea, additive: Boolean) {
        val screen = context.current.loaded?.game?.screen ?: return
        context.update { state ->
            val touched = state.slots.filterTo(mutableSetOf()) { id -> state.layout[id]?.let { area.touches(id, it, screen) } == true }
            state.copy(selection = if (additive) state.selection + touched else touched)
        }
    }

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
        val origin = context.current.loaded?.game?.screen?.crosshair ?: SlotPosition(0, 0)
        val target = SlotPosition(snapped(lead.x + session.dx, origin.x), snapped(lead.y + session.dy, origin.y))
        val moved = session.origin.moved(context.current.selection, target.x - lead.x, target.y - lead.y)
        val (layout, guides) = snappedToSlots(moved)
        context.update { it.copy(layout = layout, snapGuides = guides) }
    }

    fun dragEnd() {
        val session = drag ?: return
        drag = null
        context.update { it.copy(snapGuides = SnapGuides.None) }
        context.commitEdit(session.origin)
    }

    /**
     * Раскладка [layout] с прилипанием группы выбранных к краям и серединам невыбранных видимых элементов и окон чата
     * и к осям прицела; прилипание сильнее привязки к сетке. Выключено — раскладка как есть.
     */
    private fun snappedToSlots(layout: SlotLayout): Pair<SlotLayout, SnapGuides> {
        val state = context.current
        val moving = layout.boundsOf(state.selection)
        if (!state.snapToSlots || moving == null) return layout to SnapGuides.None
        val crosshair = state.loaded?.game?.screen?.crosshair ?: SlotPosition(0, 0)
        val lines = layout.snapLines(state.slots, state.selection, movingChat = null, crosshair)
        val result = snapToLines(moving, lines.x, lines.y, state.snapDistance)
        return layout.moved(state.selection, result.dx.roundToInt(), result.dy.roundToInt()) to result.guides
    }

    fun nudge(dx: Int, dy: Int, byGrid: Boolean) {
        val step = if (byGrid) context.current.gridSnapStep.takeIf { it > 0 } ?: NUDGE_WITHOUT_SNAP else 1
        val selection = context.current.selection
        context.editLayout { it.moved(selection, dx * step, dy * step) }
    }

    fun setPosition(id: SlotId, position: SlotPosition) = context.editLayout { it.withPosition(id, position) }

    fun undo() = context.update {
        val previous = it.history.lastOrNull() ?: return@update it
        it.copy(layout = previous, history = it.history.dropLast(1), future = it.future + it.layout, activePreset = null)
    }

    fun redo() = context.update {
        val next = it.future.lastOrNull() ?: return@update it
        it.copy(layout = next, history = it.history + it.layout, future = it.future.dropLast(1), activePreset = null)
    }

    fun setGrid(step: GridStep) = context.update { it.copy(grid = step) }

    fun setSnapToGrid(enabled: Boolean) = context.update { it.copy(snapToGrid = enabled) }

    fun setSnapStep(step: Int) = context.update { it.copy(snapStep = step) }

    fun setSnapToSlots(enabled: Boolean) = context.update { it.copy(snapToSlots = enabled) }

    fun setSnapDistance(distance: Int) = context.update { it.copy(snapDistance = distance) }

    fun align(edge: AlignEdge, anchor: SlotId?) = editSelection { layout, selection -> layout.aligned(selection, edge, anchor) }

    fun distribute(horizontal: Boolean) {
        val gap = context.current.distributeGap(horizontal)
        editSelection { layout, selection -> layout.distributed(selection, horizontal, gap) }
    }

    fun setDistributeGap(gap: Int, horizontal: Boolean) = context.update {
        if (horizontal) it.copy(distributeGapX = gap) else it.copy(distributeGapY = gap)
    }

    private fun editSelection(change: (SlotLayout, Set<SlotId>) -> SlotLayout) {
        val selection = context.current.selection
        context.editLayout { change(it, selection) }
    }

    /** Глазик на иконке: скрыть или показать элемент в игре. */
    fun toggleVisibility(id: SlotId) = context.editLayout { it.withVisibility(id, !it.isVisible(id)) }

    /** Переключает «Настройку быстрых слотов»; элементы, пропавшие с экрана, снимаются с выбора. */
    fun setCustomQuickSlots(enabled: Boolean) {
        context.editLayout { it.withCustomQuickSlots(enabled) }
        context.update { state -> state.copy(selection = state.selection.filterTo(mutableSetOf()) { it.isActiveWith(enabled) }) }
    }

    fun setLabel(id: SlotId, chars: Int?) {
        if (SlotLabel.allowedFor(id)) context.editLayout { it.withLabel(id, chars) }
    }

    /** Привязка к узлам с шагом [SlotsState.gridSnapStep], отсчитанным от прицела [origin]; выключена — без привязки. */
    private fun snapped(value: Float, origin: Int): Int {
        val step = context.current.gridSnapStep
        return if (step > 0) origin + ((value - origin) / step).roundToInt() * step else value.roundToInt()
    }

    private fun Set<SlotId>.toggled(id: SlotId, additive: Boolean): Set<SlotId> = when {
        !additive -> setOf(id)
        id in this -> this - id
        else -> this + id
    }
}
