package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.layout.SlotPosition
import com.layzen.bdotools.feature.uieditor.domain.screen.ScreenSpace
import com.layzen.bdotools.feature.uieditor.presentation.state.SkillDrag
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/** Иконка навыка, который тащат Shift+ЛКМ, — под курсором, полупрозрачная, размером со слот, из которого её взяли. */
@Composable
internal fun SkillDragGhost(drag: SkillDrag, icon: ImageBitmap, screen: ScreenSpace, scale: Float) {
    val id = drag.from
    val position = SlotPosition(drag.x.roundToInt(), drag.y.roundToInt())
    val density = LocalDensity.current
    Image(
        icon,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .offset { IntOffset((screen.iconLeft(id, position) * scale).roundToInt(), (screen.iconTop(id, position) * scale).roundToInt()) }
            .size(with(density) { (id.width * scale).toDp() }, with(density) { (id.height * scale).toDp() })
            .alpha(SlotsDimens.SKILL_DRAG_ALPHA)
            .border(SlotsDimens.selectedSlotBorder, AppTheme.colors.accentGold),
    )
}
