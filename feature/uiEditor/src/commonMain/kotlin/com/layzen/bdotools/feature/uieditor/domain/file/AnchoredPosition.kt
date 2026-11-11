package com.layzen.bdotools.feature.uieditor.domain.file

import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.screen.SlotAnchor
import com.layzen.bdotools.feature.uieditor.domain.screen.position

/** Центр элемента как смещение [x], [y] от якоря [anchor], в UI-px. Не зависит от монитора. */
data class AnchoredPosition(val anchor: SlotAnchor, val x: Int, val y: Int) {

    /** Позиция на экране [screen] — от центра экрана. */
    fun placeOn(screen: ScreenSpace): SlotPosition = anchor.point(screen).movedBy(x, y)

    companion object {
        /** Запоминает [position] от ближайшего якоря экрана [screen]. */
        fun of(position: SlotPosition, screen: ScreenSpace): AnchoredPosition {
            val anchor = SlotAnchor.nearest(position, screen)
            val point = anchor.point(screen)
            return AnchoredPosition(anchor, position.x - point.x, position.y - point.y)
        }
    }
}
