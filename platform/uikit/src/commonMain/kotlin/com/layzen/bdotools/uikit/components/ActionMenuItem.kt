package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/** Пункт меню действий [HoverActionsBox]: кликается вся строка, подсвечивается скруглённая плашка. */
@Composable
fun ActionMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val dimens = AppTheme.dimens
    // Кликается вся строка, а подсветка — скруглённая плашка с отступом, чтобы не прилипать к разделителям
    Box(
        modifier
            .fillMaxWidth()
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = dimens.spaceXs, vertical = dimens.actionMenuItemInset),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = dimens.actionMenuItemHeight - dimens.actionMenuItemInset * 2)
                .background(if (hovered && enabled) colors.backgroundSelected else Color.Transparent, AppShapes.MenuItem)
                .padding(horizontal = dimens.spaceS),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text,
                style = AppTheme.typography.bodyBold,
                color = when {
                    !enabled -> colors.textDisabled
                    hovered -> colors.accentGold
                    else -> colors.textPrimary
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
