package com.layzen.bdotools.feature.portraitconstructor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.ImagePlacement
import com.layzen.bdotools.feature.portraitconstructor.domain.PortraitGrid

/**
 * Подгонка картинки под маску: картинка, сетка ячеек и её текущее положение.
 * [disabledCells] — выключенные ячейки: портреты этих персонажей не меняются.
 */
data class EditorState(
    val target: ReplaceTarget,
    val image: ImageBitmap,
    val grid: PortraitGrid,
    val initialPlacement: ImagePlacement,
    val placement: ImagePlacement = initialPlacement,
    val disabledCells: Set<Int> = emptySet(),
) {
    /** Включает или выключает ячейку; у маски одного портрета выключать нечего. */
    fun toggledCell(index: Int): EditorState {
        if (target !is ReplaceTarget.All) return this
        return copy(disabledCells = if (index in disabledCells) disabledCells - index else disabledCells + index)
    }
}
