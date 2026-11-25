package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Перестановка пунктов списка мышью: пункт тянут за значок [DragHandle] (⠿), он едет за курсором поверх соседей,
 * а когда его отпустили, список узнаёт новый порядок через `onMove(откуда, куда)`.
 * Пункты стоят столбцом подряд; каждому — [reorderItem] с его номером.
 */
class ReorderState internal constructor() {
    internal var onMove: (from: Int, to: Int) -> Unit = { _, _ -> }
    internal val heights = mutableStateMapOf<Int, Int>()

    /** Номер пункта, который тянут сейчас; `null` — ничего не тянут. */
    var dragged by mutableStateOf<Int?>(null)
        private set

    internal var offset by mutableStateOf(0f)
        private set

    internal fun start(index: Int) {
        dragged = index
        offset = 0f
    }

    internal fun drag(delta: Float) {
        offset += delta
    }

    /**
     * Сдвиг пункта [index], пока тянут другой: пункты между местом тянутого и тем, куда он встанет, уступают ему место
     * на его высоту — вверх, если его тянут вниз, и вниз, если вверх.
     */
    internal fun shiftOf(index: Int): Float {
        val from = dragged ?: return 0f
        if (index == from) return 0f
        val to = target(from)
        val height = heights[from]?.toFloat() ?: return 0f
        return when {
            index in from + 1..to -> -height
            index in to until from -> height
            else -> 0f
        }
    }

    internal fun end() {
        val from = dragged ?: return
        val to = target(from)
        dragged = null
        offset = 0f
        if (to != from) onMove(from, to)
    }

    /** Куда встанет пункт [from], если отпустить его сейчас: по высотам пунктов, через которые он проехал. */
    internal fun target(from: Int): Int {
        var index = from
        var left = offset
        while (left > 0 && heights.containsKey(index + 1)) {
            val next = heights.getValue(index + 1)
            if (left < next / 2f) break
            left -= next
            index++
        }
        while (left < 0 && index > 0 && heights.containsKey(index - 1)) {
            val previous = heights.getValue(index - 1)
            if (-left < previous / 2f) break
            left += previous
            index--
        }
        return index
    }
}

/** Состояние перестановки списка; [onMove] — пункт `from` отпустили на месте `to`. */
@Composable
fun rememberReorderState(onMove: (from: Int, to: Int) -> Unit): ReorderState {
    val state = remember { ReorderState() }
    val current by rememberUpdatedState(onMove)
    state.onMove = { from, to -> current(from, to) }
    return state
}

/**
 * Пункт [index] списка с перестановкой: запоминает свою высоту, а пока его тянут — едет за курсором поверх соседей.
 * Соседи, мимо которых его протянули, плавно сдвигаются и освобождают место, куда он встанет.
 */
fun Modifier.reorderItem(state: ReorderState, index: Int): Modifier = composed {
    // отпустили — соседи встают на место сразу: список уже в новом порядке, анимация увела бы их обратно
    val shift by animateFloatAsState(state.shiftOf(index), if (state.dragged == null) snap() else tween(SHIFT_MS))
    onSizeChanged { state.heights[index] = it.height }
        .zIndex(if (state.dragged == index) 1f else 0f)
        .graphicsLayer { translationY = if (state.dragged == index) state.offset else shift }
}

/** Как долго сосед уступает место тянутому пункту. */
private const val SHIFT_MS = 150

/**
 * Значок ⠿ (две колонки по три точки) в начале пункта, который можно перетаскивать: за него пункт и тянут.
 * Точки нарисованы — в шрифте web-сборки нет символа ⠿.
 */
@Composable
fun DragHandle(state: ReorderState, index: Int, modifier: Modifier = Modifier) {
    val color = AppTheme.colors.textMuted
    val dimens = AppTheme.dimens
    Canvas(
        modifier
            .size(width = dimens.dragHandleWidth, height = dimens.dragHandleHeight)
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(state, index) {
                detectVerticalDragGestures(
                    onDragStart = { state.start(index) },
                    onDragEnd = { state.end() },
                    onDragCancel = { state.end() },
                ) { change, delta ->
                    change.consume()
                    state.drag(delta)
                }
            },
    ) {
        val radius = dimens.dragHandleDot.toPx() / 2
        for (column in 0..1) for (row in 0..2) {
            val x = radius + column * (size.width - 2 * radius)
            val y = radius + row * (size.height - 2 * radius) / 2
            drawCircle(color, radius, Offset(x, y))
        }
    }
}
