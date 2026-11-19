package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.layzen.bdotools.feature.comboassist.domain.combo.CellAt

/**
 * Перетаскивание ячейки.
 *
 * @property from какую ячейку тянут
 * @property start центр ячейки в начале
 * @property offset насколько её сдвинули
 */
internal data class CellDrag(val from: CellAt, val start: Offset, val offset: Offset) {
    /** Где сейчас курсор. */
    val pointer: Offset get() = start + offset
}

/** Состояние и обработчики перетаскивания для ячеек холста. */
internal class CanvasDrag(
    val current: CellDrag?,
    val target: CellDrop?,
    val onStart: (CellAt) -> Unit,
    val onMove: (Offset) -> Unit,
    val onEnd: () -> Unit,
    val onCancel: () -> Unit,
    val place: (CellAt, Rect) -> Unit,
)
