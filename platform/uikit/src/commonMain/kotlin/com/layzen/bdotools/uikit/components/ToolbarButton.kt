package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Кнопка-иконка панели инструментов с подсказкой [description] при наведении.
 * [selected] — включённый режим (золотая иконка), [enabled] = `false` — серая иконка, клик не срабатывает.
 * [icon] — белый силуэт, цвет ему задаёт кнопка.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarButton(
    icon: Painter,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val gap = with(LocalDensity.current) { dimens.spaceXs.roundToPx() }
    TooltipBox(
        positionProvider = remember(gap) { BelowAnchorPositionProvider(gap) },
        tooltip = {
            Text(
                description,
                style = AppTheme.typography.caption,
                color = colors.textPrimary,
                modifier = Modifier
                    .background(colors.backgroundRaised)
                    .border(dimens.borderWidth, colors.border)
                    .padding(horizontal = dimens.spaceS, vertical = dimens.spaceXs),
            )
        },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        SquareIconButton(onClick, enabled = enabled, selected = selected) {
            Image(
                icon,
                contentDescription = description,
                modifier = Modifier.size(dimens.iconSize),
                colorFilter = ColorFilter.tint(
                    when {
                        !enabled -> colors.textDisabled
                        selected -> colors.accentGold
                        else -> colors.textButton
                    },
                ),
            )
        }
    }
}
