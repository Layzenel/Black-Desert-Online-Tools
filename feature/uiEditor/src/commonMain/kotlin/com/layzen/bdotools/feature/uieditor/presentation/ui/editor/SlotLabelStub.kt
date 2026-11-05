package com.layzen.bdotools.feature.uieditor.presentation.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.layzen.bdotools.feature.uieditor.domain.ScreenSpace
import com.layzen.bdotools.feature.uieditor.domain.SlotId
import com.layzen.bdotools.feature.uieditor.domain.SlotLabel
import com.layzen.bdotools.feature.uieditor.domain.SlotPosition
import com.layzen.bdotools.feature.uieditor.presentation.ui.SlotsDimens
import com.layzen.bdotools.uikit.theme.AppTheme
import kotlin.math.roundToInt

/**
 * Место, которое займёт надпись слота в игре: полоса шириной [chars] символов под рамкой, по центру слота.
 * Клики проходят сквозь неё к холсту. У скрытого в игре слота приглушена, как и сам слот.
 */
@Composable
internal fun SlotLabelStub(id: SlotId, position: SlotPosition, screen: ScreenSpace, scale: Float, chars: Int, visible: Boolean) {
    val width = chars * SlotLabel.CHAR_WIDTH
    val left = screen.iconLeft(id, position) + (id.width - width) / 2
    val top = screen.iconTop(id, position) + id.height + SlotLabel.GAP
    val density = LocalDensity.current
    Box(
        Modifier
            .offset { IntOffset((left * scale).roundToInt(), (top * scale).roundToInt()) }
            .size(with(density) { (width * scale).toDp() }, with(density) { (SlotLabel.HEIGHT * scale).toDp() })
            .alpha(if (visible) 1f else SlotsDimens.HIDDEN_ALPHA)
            .background(AppTheme.colors.textPrimary.copy(alpha = SlotsDimens.LABEL_FILL_ALPHA))
            .border(SlotsDimens.slotBorder, AppTheme.colors.textPrimary),
    )
}
