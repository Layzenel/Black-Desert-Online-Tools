package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Тонкая полоса прокрутки у правого края: дорожка и бегунок по [state].
 * Ставится **перед** `verticalScroll(state)`, чтобы рисоваться в видимой области, а не прокручиваться
 * вместе с содержимым. Пока прокручивать нечего, полосы нет.
 */
@Composable
fun Modifier.verticalScrollbar(state: ScrollState): Modifier {
    val colors = AppTheme.colors
    val width = AppTheme.dimens.scrollbarWidth
    val inset = AppTheme.dimens.scrollbarInset
    return drawWithContent {
        drawContent()
        val hidden = state.maxValue
        if (hidden <= 0 || hidden == Int.MAX_VALUE) return@drawWithContent
        val barWidth = width.toPx()
        val x = size.width - barWidth - inset.toPx()
        val track = size.height
        val thumb = (track * track / (track + hidden)).coerceAtLeast(barWidth * 2)
        val top = (track - thumb) * state.value / hidden
        val corner = CornerRadius(barWidth / 2)
        drawRoundRect(colors.track, Offset(x, 0f), Size(barWidth, track), corner)
        drawRoundRect(colors.textMuted, Offset(x, top), Size(barWidth, thumb), corner)
    }
}
