package com.layzen.bdotools.feature.portraitconstructor.presentation.state

import androidx.compose.ui.graphics.ImageBitmap
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.ImagePlacement
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitGrid

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

    /**
     * Масштаб [scale] в пределах шкалы зума [ZOOM_PERCENT_RANGE] от исходного: колесо мыши не уводит
     * картинку дальше, чем позволяет слайдер.
     */
    fun zoomedTo(scale: Float): EditorState {
        val base = initialPlacement.scale
        val bounded = scale.coerceIn(base * ZOOM_PERCENT_RANGE.first / PERCENT, base * ZOOM_PERCENT_RANGE.last / PERCENT)
        return copy(placement = placement.zoomedTo(bounded))
    }

    companion object {
        /** Шкала зума, % от масштаба, при котором картинка закрывает маску. */
        val ZOOM_PERCENT_RANGE = 10..400

        private const val PERCENT = 100f
    }
}
