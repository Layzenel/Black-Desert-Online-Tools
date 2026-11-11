package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Пункт главного меню: иконка, жирное название, плашка NEW/HOT и горячая клавиша справа.
 * [selected] — коричневая подложка с рамкой, [highlighted] — золотой текст (новый или важный раздел).
 */
@Composable
fun MenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    hotkey: String? = null,
    badge: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    highlighted: Boolean = false,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val textColor = if (highlighted) colors.accentGold else colors.textPrimary

    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = dimens.menuItemHeight)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Tab, onClick = onClick)
            .background(
                when {
                    selected -> colors.backgroundSelected
                    hovered -> colors.backgroundItem
                    else -> colors.backgroundRaised
                },
            )
            .border(dimens.borderWidth, if (selected) colors.borderSelected else colors.border)
            .padding(horizontal = dimens.spaceM, vertical = dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceM),
    ) {
        if (icon != null) {
            // Не меньше квадрата иконки, но шире, если в слоте больше (отметка и портрет)
            Box(Modifier.sizeIn(minWidth = dimens.menuIconBoxSize, minHeight = dimens.menuIconBoxSize), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(LocalContentColor provides textColor) { icon() }
            }
        }
        Text(
            text = text,
            style = AppTheme.typography.menuItem,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (badge != null) badge()
        if (hotkey != null) {
            Text(hotkey, style = AppTheme.typography.bodyBold, color = colors.textBeige)
        }
    }
}
