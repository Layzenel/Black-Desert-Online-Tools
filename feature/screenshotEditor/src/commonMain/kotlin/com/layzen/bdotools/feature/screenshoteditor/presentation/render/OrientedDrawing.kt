package com.layzen.bdotools.feature.screenshoteditor.presentation.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.layzen.bdotools.feature.screenshoteditor.domain.edit.Orientation

/**
 * Рисует [block] в координатах картинки [width]×[height] так, как её видно после [orientation]: сначала отражение,
 * потом поворот — то же преобразование, что [Orientation.toDisplay].
 */
internal fun DrawScope.oriented(orientation: Orientation, width: Float, height: Float, block: DrawScope.() -> Unit) {
    withTransform({
        when (orientation.quarterTurns) {
            1 -> translate(height, 0f)
            2 -> translate(width, height)
            3 -> translate(0f, width)
        }
        if (orientation.quarterTurns != 0) rotate(orientation.quarterTurns * 90f, Offset.Zero)
        if (orientation.mirrored) {
            translate(width, 0f)
            scale(-1f, 1f, Offset.Zero)
        }
    }, block)
}
