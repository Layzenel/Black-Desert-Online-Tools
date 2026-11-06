package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.layzen.bdotools.uikit.theme.AppTheme

/** Подсказка [text] под [content], пока над ним курсор: как у кнопок панели инструментов. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoverTooltip(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val gap = with(LocalDensity.current) { dimens.spaceXs.roundToPx() }
    TooltipBox(
        positionProvider = remember(gap) { BelowAnchorPositionProvider(gap) },
        tooltip = {
            Text(
                text,
                style = AppTheme.typography.caption,
                color = colors.textPrimary,
                modifier = Modifier
                    .widthIn(max = dimens.tooltipMaxWidth)
                    .background(colors.backgroundRaised)
                    .border(dimens.borderWidth, colors.border)
                    .padding(horizontal = dimens.spaceS, vertical = dimens.spaceXs),
            )
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}
