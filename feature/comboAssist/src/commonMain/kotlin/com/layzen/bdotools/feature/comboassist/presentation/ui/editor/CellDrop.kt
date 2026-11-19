package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt
import com.layzen.bdotools.feature.comboassist.domain.combo.DropSide
import kotlin.math.abs

/**
 * Куда упадёт перетаскиваемая ячейка.
 *
 * @property at ячейка, к которой бросают; та же, что тянут, — ячейка выходит из развилки влево или вправо
 * @property side сторона этой ячейки, к которой ближе курсор
 */
internal data class CellDrop(val at: CellAt, val side: DropSide) {
    companion object {
        /**
         * Место для курсора [pointer]: ближайшая ячейка из [bounds], в границы которой, расширенные на [slop], он попал.
         * Сама ячейка [from] — цель, только если она в развилке ([fromInFork]), курсор вышел за её границы и сторона —
         * слева или справа: так её выносят из развилки.
         */
        fun find(bounds: Map<CellAt, Rect>, from: CellAt, fromInFork: Boolean, pointer: Offset, slop: Float): CellDrop? {
            val (at, rect) = bounds.entries.filter { it.value.inflate(slop).contains(pointer) }
                .minByOrNull { (it.value.center - pointer).getDistanceSquared() } ?: return null
            val drop = of(at, rect, pointer)
            if (at != from) return drop
            val leavesFork = fromInFork && !rect.contains(pointer) && drop.side in setOf(DropSide.Before, DropSide.After)
            return drop.takeIf { leavesFork }
        }

        /** Место для курсора [pointer] над ячейкой [at] с границами [rect]: сторона — та, к краю которой он ближе. */
        fun of(at: CellAt, rect: Rect, pointer: Offset): CellDrop {
            val dx = (pointer.x - rect.center.x) / rect.width
            val dy = (pointer.y - rect.center.y) / rect.height
            val side = when {
                abs(dy) > abs(dx) -> if (dy < 0) DropSide.Above else DropSide.Below
                else -> if (dx < 0) DropSide.Before else DropSide.After
            }
            return CellDrop(at, side)
        }
    }
}
