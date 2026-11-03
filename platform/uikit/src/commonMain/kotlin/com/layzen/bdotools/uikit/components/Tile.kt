package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Плитка сетки (как карточки персонажей): серый градиент снизу вверх,
 * выбранная — светло-золотая рамка. Подходит для предметов, классов, локаций.
 */
@Composable
fun Tile(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(interaction, indication = null, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .background(Brush.verticalGradient(listOf(colors.backgroundSurface, colors.backgroundItem)))
            .border(
                AppTheme.dimens.selectedBorderWidth,
                when {
                    selected -> colors.accentGoldPale
                    hovered -> colors.textMuted
                    else -> colors.border
                },
            ),
        content = content,
    )
}
