package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * Перетаскивание умения из списка на холст. Список сообщает, какое умение тянут и где курсор ([move]), холст по этому
 * ищет ячейку-цель ([CellDrop.find]) и, когда умение отпустили ([released]), вставляет его и сбрасывает ([clear]).
 * Координаты — окна, как у границ ячеек холста.
 *
 * @property skillNo умение, которое тянут, или `null`
 * @property pointer где курсор
 * @property released кнопку отпустили: холст должен вставить умение
 */
internal class PaletteDrag {
    var skillNo by mutableStateOf<Int?>(null)
        private set
    var pointer by mutableStateOf(Offset.Zero)
        private set
    var released by mutableStateOf(false)
        private set

    /** Умение [skillNo] тянут, курсор в [pointer]. */
    fun move(skillNo: Int, pointer: Offset) {
        this.skillNo = skillNo
        this.pointer = pointer
    }

    /** Кнопку отпустили: холст вставит умение, если курсор над ячейкой. */
    fun release() { if (skillNo != null) released = true }

    /** Сбросить перетаскивание: вставлено или отменено. */
    fun clear() {
        skillNo = null
        released = false
    }
}
