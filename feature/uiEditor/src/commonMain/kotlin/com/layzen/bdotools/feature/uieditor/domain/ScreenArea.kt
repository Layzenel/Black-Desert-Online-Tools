package com.layzen.bdotools.feature.uieditor.domain

import kotlin.math.max
import kotlin.math.min

/** Прямоугольник на экране игры в UI-px от левого верхнего угла — например, рамка обводки. */
data class ScreenArea(val left: Double, val top: Double, val right: Double, val bottom: Double) {

    /** Задевает ли обводка рамку элемента [id] с центром в [position] на экране [screen]. */
    fun touches(id: SlotId, position: SlotPosition, screen: ScreenSpace): Boolean {
        val frameLeft = screen.iconLeft(id, position)
        val frameTop = screen.iconTop(id, position)
        return frameLeft < right && frameLeft + id.width > left && frameTop < bottom && frameTop + id.height > top
    }

    /** Задевает ли обводка окно чата [chat] на экране [screen]. */
    fun touches(chat: ChatWindow, screen: ScreenSpace): Boolean {
        val frameLeft = chat.left(screen)
        val frameTop = chat.top(screen)
        return frameLeft < right && frameLeft + chat.width > left && frameTop < bottom && frameTop + chat.height > top
    }

    companion object {
        /** Прямоугольник между двумя углами в любом порядке: обводку можно тянуть в любую сторону. */
        fun between(x1: Double, y1: Double, x2: Double, y2: Double) = ScreenArea(min(x1, x2), min(y1, y2), max(x1, x2), max(y1, y2))
    }
}
