package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.arrangement.AlignEdge
import com.layzen.bdotools.feature.uieditor.domain.arrangement.SnapGuides
import com.layzen.bdotools.feature.uieditor.domain.arrangement.aligned
import com.layzen.bdotools.feature.uieditor.domain.arrangement.distributed
import com.layzen.bdotools.feature.uieditor.domain.arrangement.distributionGap
import com.layzen.bdotools.feature.uieditor.domain.arrangement.snapLines
import com.layzen.bdotools.feature.uieditor.domain.arrangement.snapToLines
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotId
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenArea
import com.layzen.bdotools.feature.uieditor.domain.screen.position
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.GridStep
import kotlin.math.roundToInt

/** Шаг стрелки с Shift, если привязка к сетке выключена. */
private const val NUDGE_WITHOUT_SNAP = 10

/** Выбор и перемещение слотов и окон чата, отмена, сетка. */
internal class EditorController(private val context: SlotsContext) {

    /** Раскладка до начала жеста слайдера промежутка «Распределить»; `null` — жеста нет. */
    private var distributeOrigin: SlotLayout? = null

    /**
     * Перетаскивание: раскладка до начала, точка схваченного под курсором ([lead]: центр слота, у окна чата —
     * левый нижний угол, как при растягивании) и накопленный сдвиг в UI-px.
     */
    private class DragSession(val origin: SlotLayout, val lead: SlotPosition) {
        var dx = 0f
        var dy = 0f
    }

    private var drag: DragSession? = null

    /** Клик по слоту: выбрать его одного, с [additive] — добавить к выбору или убрать из него. */
    fun select(id: SlotId, additive: Boolean) = context.update {
        it.copy(selection = it.selection.toggled(id, additive), selectedChats = if (additive) it.selectedChats else emptySet())
    }

    /** Клик по окну чата: выбрать его одно, с [additive] — добавить к выбору или убрать из него. */
    fun selectChat(window: Int, additive: Boolean) = context.update {
        it.copy(selectedChats = it.selectedChats.toggled(window, additive), selection = if (additive) it.selection else emptySet())
    }

    /** Снимает выбор со слотов и окон чата. */
    fun clearSelection() = context.update { it.copy(selection = emptySet(), selectedChats = emptySet()) }

    /** Выбирает всё, что задела обводка; с [additive] — вместе с уже выбранным. */
    fun selectArea(area: ScreenArea, additive: Boolean) {
        val screen = context.current.loaded?.game?.screen ?: return
        context.update { state ->
            val touched = state.slots.filterTo(mutableSetOf()) { id -> state.layout[id]?.let { area.touches(id, it, screen) } == true }
            val chats = state.layout.chats.filter { (window, chat) -> state.layout.isChatShown(window) && area.touches(chat, screen) }.keys
            state.copy(
                selection = if (additive) state.selection + touched else touched,
                selectedChats = if (additive) state.selectedChats + chats else chats,
            )
        }
    }

    /** Схватить слот: выбранная группа двигается целиком, невыбранный слот без Ctrl выбирается один. */
    fun dragStart(id: SlotId, additive: Boolean) {
        val lead = context.current.layout[id] ?: return
        val grabbed = id in context.current.selection
        context.update { state ->
            when {
                grabbed -> state
                additive -> state.copy(selection = state.selection + id)
                else -> state.copy(selection = setOf(id), selectedChats = emptySet())
            }
        }
        drag = DragSession(context.current.layout, lead)
    }

    /** Схватить окно чата — так же, как слот [dragStart]. */
    fun dragChatStart(window: Int, additive: Boolean) {
        val bounds = context.current.layout.chats[window]?.bounds ?: return
        val grabbed = window in context.current.selectedChats
        context.update { state ->
            when {
                grabbed -> state
                additive -> state.copy(selectedChats = state.selectedChats + window)
                else -> state.copy(selection = emptySet(), selectedChats = setOf(window))
            }
        }
        drag = DragSession(context.current.layout, SlotPosition(bounds.left.roundToInt(), bounds.bottom.roundToInt()))
    }

    /** Сдвиг курсора с прошлого вызова. С привязкой к сетке на узел сетки встаёт слот под курсором. */
    fun dragBy(dx: Float, dy: Float) {
        val session = drag ?: return
        session.dx += dx
        session.dy += dy
        val lead = session.lead
        val origin = context.current.loaded?.game?.screen?.crosshair ?: SlotPosition(0, 0)
        val target = SlotPosition(snapped(lead.x + session.dx, origin.x), snapped(lead.y + session.dy, origin.y))
        val state = context.current
        val moved = session.origin.moved(state.selection, state.selectedChats, target.x - lead.x, target.y - lead.y)
        val (layout, guides) = snappedToSlots(moved)
        context.update { it.copy(layout = layout, snapGuides = guides) }
    }

    /** Конец перетаскивания: убирает направляющие и заносит перенос в историю «Отменить». */
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
        val moving = layout.boundsOf(state.selection, state.selectedChats)
        if (!state.snapToSlots || moving == null) return layout to SnapGuides.None
        val crosshair = state.loaded?.game?.screen?.crosshair ?: SlotPosition(0, 0)
        val lines = layout.snapLines(state.slots, state.selection, state.selectedChats, crosshair)
        val result = snapToLines(moving, lines.x, lines.y, state.snapDistance)
        return layout.moved(state.selection, state.selectedChats, result.dx.roundToInt(), result.dy.roundToInt()) to result.guides
    }

    /** Сдвигает выбранное на шаг в [dx], [dy]; [byGrid] — на шаг сетки, иначе на 1 UI-px. */
    fun nudge(dx: Int, dy: Int, byGrid: Boolean) {
        val step = if (byGrid) context.current.gridSnapStep.takeIf { it > 0 } ?: NUDGE_WITHOUT_SNAP else 1
        val state = context.current
        context.editLayout { it.moved(state.selection, state.selectedChats, dx * step, dy * step) }
    }

    /** Ставит слот [id] в позицию [position], например из полей ввода координат. */
    fun setPosition(id: SlotId, position: SlotPosition) = context.editLayout { it.withPosition(id, position) }

    /** Возвращает раскладку, какой она была до последней правки. */
    fun undo() = context.update {
        val previous = it.history.lastOrNull() ?: return@update it
        it.copy(layout = previous, history = it.history.dropLast(1), future = it.future + it.layout, activePreset = null)
    }

    /** Повторяет последнюю отменённую правку. */
    fun redo() = context.update {
        val next = it.future.lastOrNull() ?: return@update it
        it.copy(layout = next, history = it.history + it.layout, future = it.future.dropLast(1), activePreset = null)
    }

    /** Выбирает вид сетки на холсте. */
    fun setGrid(step: GridStep) = context.update { it.copy(grid = step) }

    /** Включает или выключает привязку к узлам сетки. */
    fun setSnapToGrid(enabled: Boolean) = context.update { it.copy(snapToGrid = enabled) }

    /** Задаёт шаг привязки к узлам сетки, UI-px. */
    fun setSnapStep(step: Int) = context.update { it.copy(snapStep = step) }

    /** Включает или выключает прилипание к соседним слотам. */
    fun setSnapToSlots(enabled: Boolean) = context.update { it.copy(snapToSlots = enabled) }

    /** Задаёт расстояние, с которого рамка прилипает к линиям, UI-px. */
    fun setSnapDistance(distance: Int) = context.update { it.copy(snapDistance = distance) }

    /** Выравнивает выбранные слоты по краю [edge]; [anchor] — слот, по которому равняются, иначе по рамке выбора. */
    fun align(edge: AlignEdge, anchor: SlotId?) = editSelection { layout, selection -> layout.aligned(selection, edge, anchor) }

    /** Равномерно расставляет выбранные слоты по горизонтали или вертикали с заданным промежутком. */
    fun distribute(horizontal: Boolean) {
        val state = context.current
        // Промежуток — текущий у группы: клик выравнивает шаг, не меняя его
        val gap = state.layout.distributionGap(state.selection, horizontal) ?: state.distributeGap(horizontal)
        editSelection { layout, selection -> layout.distributed(selection, horizontal, gap) }
    }

    /**
     * Запоминает промежуток для расстановки по горизонтали ([horizontal]) или по вертикали и сразу расставляет
     * выбранное с ним. Каждый шаг жеста слайдера считается от раскладки на его начало, в «Отменить» жест
     * попадает одним шагом ([endDistributeGap]).
     */
    fun setDistributeGap(gap: Int, horizontal: Boolean) {
        context.update { if (horizontal) it.copy(distributeGapX = gap) else it.copy(distributeGapY = gap) }
        val state = context.current
        if (state.selection.isEmpty()) return
        val start = distributeOrigin ?: state.layout.also { distributeOrigin = it }
        context.update { it.copy(layout = start.distributed(state.selection, horizontal, gap)) }
    }

    /** Конец жеста слайдера промежутка: заносит расстановку в историю «Отменить». */
    fun endDistributeGap() {
        val previous = distributeOrigin ?: return
        distributeOrigin = null
        context.commitEdit(previous)
    }

    private fun editSelection(change: (SlotLayout, Set<SlotId>) -> SlotLayout) {
        val selection = context.current.selection
        context.editLayout { change(it, selection) }
    }

    /** Глазик на иконке: скрыть или показать элемент в игре. */
    fun toggleVisibility(id: SlotId) = context.editLayout { it.withVisibility(id, !it.isVisible(id)) }

    /** Прячет или показывает все выбранные элементы одним шагом «Отменить». */
    fun setSelectionVisibility(visible: Boolean) {
        val selection = context.current.selection
        context.editLayout { layout -> selection.fold(layout) { result, id -> result.withVisibility(id, visible) } }
    }

    /** Переключает «Настройку быстрых слотов»; элементы, пропавшие с экрана, снимаются с выбора. */
    fun setCustomQuickSlots(enabled: Boolean) {
        context.editLayout { it.withCustomQuickSlots(enabled) }
        dropInactiveFromSelection()
    }

    /** Переключает «Разделение HP»; элементы, пропавшие с экрана, снимаются с выбора. */
    fun setSplitHp(enabled: Boolean) {
        context.editLayout { it.withSplitHp(enabled) }
        dropInactiveFromSelection()
    }

    private fun dropInactiveFromSelection() = context.update { state ->
        state.copy(selection = state.selection.filterTo(mutableSetOf()) { state.layout.isActive(it) })
    }

    /** Ставит слоту [id] заглушку надписи шириной [chars] символов; `null` убирает её. */
    fun setLabel(id: SlotId, chars: Int?) {
        if (SlotLabel.allowedFor(id)) context.editLayout { it.withLabel(id, chars) }
    }

    /** Привязка к узлам с шагом [SlotsState.gridSnapStep], отсчитанным от прицела [origin]; выключена — без привязки. */
    private fun snapped(value: Float, origin: Int): Int {
        val step = context.current.gridSnapStep
        return if (step > 0) origin + ((value - origin) / step).roundToInt() * step else value.roundToInt()
    }

    private fun <T> Set<T>.toggled(id: T, additive: Boolean): Set<T> = when {
        !additive -> setOf(id)
        id in this -> this - id
        else -> this + id
    }
}
