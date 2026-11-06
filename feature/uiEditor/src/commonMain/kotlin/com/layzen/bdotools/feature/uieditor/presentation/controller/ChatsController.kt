package com.layzen.bdotools.feature.uieditor.presentation.controller

import com.layzen.bdotools.feature.uieditor.domain.ChatWindow
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.SlotLayout
import com.layzen.bdotools.feature.uieditor.domain.SnapGuides
import com.layzen.bdotools.feature.uieditor.domain.SnapLines
import com.layzen.bdotools.feature.uieditor.domain.nearestLine
import com.layzen.bdotools.feature.uieditor.domain.snapLines
import com.layzen.bdotools.feature.uieditor.domain.snapToLines
import com.layzen.bdotools.feature.uieditor.presentation.SlotsContext
import com.layzen.bdotools.feature.uieditor.presentation.state.SlotsDialog
import kotlin.math.roundToInt

/**
 * Окна чата в редакторе: перемещение, растягивание и размер, видимость, вынос вкладки в отдельное окно и обратно.
 * Перемещение и растягивание слушаются тех же привязок, что и слоты: к сетке от прицела и к элементам.
 */
internal class ChatsController(private val context: SlotsContext) {

    /** Раскладка до начала жеста: от неё считается сдвиг, и она уйдёт в «Отменить», когда жест закончится. */
    private var origin: SlotLayout? = null

    /**
     * Перетаскивание окна [window] на [dx], [dy] UI-px от начала жеста. С привязкой к сетке на узел встаёт левый нижний
     * угол, при прилипании — ближайший край или середина рамки.
     */
    fun move(window: Int, dx: Int, dy: Int) = edit(window) { chat, screen ->
        val bounds = chat.bounds
        val crosshair = screen.crosshair
        var moved = chat.movedBy(gridShift(bounds.left, dx, crosshair.x), gridShift(bounds.bottom, dy, crosshair.y))
        var guides = SnapGuides.None
        lines(window, screen)?.let { lines ->
            val result = snapToLines(moved.bounds, lines.x, lines.y, context.current.snapDistance)
            moved = moved.movedBy(result.dx.roundToInt(), result.dy.roundToInt())
            guides = result.guides
        }
        moved to guides
    }

    /** Растягивание окна [window] за правый верхний угол на [dx], [dy] UI-px от начала жеста; угол слушается привязок. */
    fun resize(window: Int, dx: Int, dy: Int) = edit(window) { chat, screen ->
        val bounds = chat.bounds
        val crosshair = screen.crosshair
        var right = bounds.right + gridShift(bounds.right, dx, crosshair.x)
        var top = bounds.top + gridShift(bounds.top, dy, crosshair.y)
        val lines = lines(window, screen)
        val distance = context.current.snapDistance
        val lineX = lines?.let { nearestLine(right, it.x, distance) }?.also { right = it }
        val lineY = lines?.let { nearestLine(top, it.y, distance) }?.also { top = it }
        chat.resizedFromTopRight((right - bounds.right).roundToInt(), (top - bounds.top).roundToInt(), screen) to SnapGuides(lineX, lineY)
    }

    fun endEdit() {
        val previous = origin ?: return
        origin = null
        context.update { it.copy(snapGuides = SnapGuides.None) }
        context.commitEdit(previous)
    }

    /** Размер окна [window], введённый руками: левый нижний угол на месте, пределы — как при растягивании. */
    fun setSize(window: Int, width: Int, height: Int) {
        val screen = context.current.loaded?.game?.screen ?: return
        context.editLayout { layout ->
            val chat = layout.chats[window] ?: return@editLayout layout
            layout.withChat(window, chat.withSize(width, height, screen))
        }
        context.update { it.copy(dialog = null) }
    }

    /** «Вынести» на плашке вкладки: окно встаёт над главным. */
    fun detach(window: Int) = context.editLayout { it.withDetachedChat(window) }

    /** «Внести» на плашке отдельного окна: оно становится вкладкой главного. */
    fun combine(window: Int) = context.editLayout { it.withCombinedChat(window) }

    /** Глазик окна: у главного чата он прячет и все остальные окна, см. [SlotLayout.isChatShown]. */
    fun toggleVisibility(window: Int) = context.editLayout { layout ->
        val chat = layout.chats[window] ?: return@editLayout layout
        layout.withChat(window, chat.copy(visible = !chat.visible))
    }


    fun openSize(window: Int) = context.update { it.copy(dialog = SlotsDialog.ChatSize(window)) }

    /** Шаг жеста: окно считается от раскладки на начало жеста, линии прилипания показываются на холсте. */
    private fun edit(window: Int, change: (ChatWindow, ScreenSpace) -> Pair<ChatWindow, SnapGuides>) {
        val screen = context.current.loaded?.game?.screen ?: return
        val start = origin ?: context.current.layout.also { origin = it }
        val chat = start.chats[window] ?: return
        val (changed, guides) = change(chat, screen)
        context.update { it.copy(layout = it.layout.withChat(window, changed), snapGuides = guides) }
    }

    /**
     * Сдвиг края [edge] на [delta] UI-px с привязкой к сетке: край встаёт на узел с шагом сетки от [origin] (ось прицела).
     * Без привязки — сдвиг как есть, чтобы окно с нечётным размером не дёргалось на полпикселя.
     */
    private fun gridShift(edge: Double, delta: Int, origin: Int): Int {
        val step = context.current.gridSnapStep
        if (step <= 0) return delta
        val snapped = origin + ((edge + delta - origin) / step).roundToInt() * step
        return (snapped - edge).roundToInt()
    }

    /** Линии прилипания для окна [window], пока прилипание к элементам включено. */
    private fun lines(window: Int, screen: ScreenSpace): SnapLines? {
        val state = context.current
        if (!state.snapToSlots) return null
        return (origin ?: state.layout).snapLines(state.slots, emptySet(), window, screen.crosshair)
    }
}
