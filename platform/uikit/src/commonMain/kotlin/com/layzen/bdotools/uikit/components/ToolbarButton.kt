package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Кнопка-иконка панели инструментов с подсказкой [description] при наведении.
 * [selected] — включённый режим (золотая иконка), [enabled] = `false` — серая иконка, клик не срабатывает.
 * [icon] — белый силуэт, цвет ему задаёт кнопка.
 */
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
    HoverTooltip(description, modifier) {
        SquareIconButton(onClick, enabled = enabled, selected = selected) {
            Image(
                icon,
                contentDescription = description,
                modifier = Modifier.size(AppTheme.dimens.iconSize),
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
