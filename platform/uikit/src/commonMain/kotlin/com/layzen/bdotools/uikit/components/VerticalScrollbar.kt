package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Полоса прокрутки у правого края: дорожка и бегунок по [state]. Тонкая, а под курсором и пока её тянут —
 * шире (`scrollbarHoverWidth`), чтобы по ней было легко попасть. Бегунок перетаскивается мышью, клик по дорожке
 * переносит бегунок туда. Нажатия на полосу не доходят до содержимого под ней.
 * Ставится **перед** `verticalScroll(state)`, чтобы рисоваться в видимой области, а не прокручиваться
 * вместе с содержимым. Пока прокручивать нечего, полосы нет.
 */
@Composable
fun Modifier.verticalScrollbar(state: ScrollState): Modifier {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    var active by remember { mutableStateOf(false) }
    // полосу убрали с экрана посреди жеста (закрыли список) — перетаскивания больше нет
    DisposableEffect(state) { onDispose { if (active) ScrollbarDrag.end() } }
    return pointerInput(state) {
        val grabWidth = (dimens.scrollbarHoverWidth + dimens.scrollbarInset * 2).toPx()
        awaitPointerEventScope {
            var dragging = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull() ?: continue
                if (dragging && !change.pressed && event.type != PointerEventType.Release) {
                    // кнопку отпустили там, где полоса этого не видела (за краем окна): жест всё равно закончен
                    dragging = false
                    ScrollbarDrag.end()
                }
                val hidden = state.maxValue
                val scrollable = hidden > 0 && hidden != Int.MAX_VALUE
                val onBar = scrollable && change.position.x >= size.width - grabWidth && event.type != PointerEventType.Exit
                val track = size.height.toFloat()
                val thumb = thumbLength(track, hidden, grabWidth)
                when {
                    event.type == PointerEventType.Press && onBar -> {
                        dragging = true
                        ScrollbarDrag.start()
                        val top = (track - thumb) * state.value / hidden
                        // Клик мимо бегунка переносит его центр под курсор, по бегунку — тянет с места
                        if (change.position.y !in top..top + thumb) {
                            state.dispatchRawDelta((change.position.y - thumb / 2) / (track - thumb) * hidden - state.value)
                        }
                        change.consume()
                    }
                    event.type == PointerEventType.Release && dragging -> {
                        dragging = false
                        ScrollbarDrag.end()
                        change.consume()
                    }
                    event.type == PointerEventType.Move && dragging -> {
                        val dy = change.position.y - change.previousPosition.y
                        state.dispatchRawDelta(dy / (track - thumb) * hidden)
                        change.consume()
                    }
                }
                active = dragging || onBar
            }
        }
    }.drawWithContent {
        drawContent()
        val hidden = state.maxValue
        if (hidden <= 0 || hidden == Int.MAX_VALUE) return@drawWithContent
        val barWidth = (if (active) dimens.scrollbarHoverWidth else dimens.scrollbarWidth).toPx()
        val x = size.width - barWidth - dimens.scrollbarInset.toPx()
        val track = size.height
        val thumb = thumbLength(track, hidden, dimens.scrollbarWidth.toPx() * 2)
        val top = (track - thumb) * state.value / hidden
        val corner = CornerRadius(barWidth / 2)
        drawRoundRect(colors.track, Offset(x, 0f), Size(barWidth, track), corner)
        drawRoundRect(if (active) colors.textBeige else colors.textMuted, Offset(x, top), Size(barWidth, thumb), corner)
    }
}

/** Длина бегунка: доля видимого от всего содержимого, но не короче [minLength]. */
private fun thumbLength(track: Float, hidden: Int, minLength: Float): Float =
    (track * track / (track + hidden)).coerceAtLeast(minLength)
