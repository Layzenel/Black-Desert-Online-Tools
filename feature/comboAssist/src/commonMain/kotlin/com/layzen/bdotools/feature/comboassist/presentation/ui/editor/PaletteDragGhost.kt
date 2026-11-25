package com.layzen.bdotools.feature.comboassist.presentation.ui.editor

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.comboassist.presentation.ui.ComboDimens
import com.layzen.bdotools.feature.comboassist.presentation.ui.skill.SkillIconView
import kotlin.math.roundToInt

/** Насколько прозрачна иконка умения под курсором, чтобы под ней была видна синяя черта цели. */
private const val GHOST_ALPHA = 0.8f

/**
 * Иконка умения, которое тянут из списка ([drag]), под курсором — чтобы было видно, что его несут на холст.
 * [origin] — где в окне начинается слой, в котором рисуется иконка (координаты [drag] — окна).
 */
@Composable
internal fun PaletteDragGhost(drag: PaletteDrag, origin: Offset, icon: (Int) -> ImageBitmap?) {
    val skillNo = drag.skillNo ?: return
    val half = with(LocalDensity.current) { ComboDimens.editorIcon.toPx() } / 2
    SkillIconView(
        icon(skillNo),
        ComboDimens.editorIcon,
        Modifier
            .offset { IntOffset((drag.pointer.x - origin.x - half).roundToInt(), (drag.pointer.y - origin.y - half).roundToInt()) }
            .alpha(GHOST_ALPHA),
    )
}
