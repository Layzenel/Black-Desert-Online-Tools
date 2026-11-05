package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Пункт меню действий [HoverActionsBox] и [ContextMenu]: кликается вся строка, подсвечивается скруглённая плашка.
 * [icon] слева и [trailingIcon] справа (например, стрелка раскрытия) — белые силуэты, цвет им задаёт пункт;
 * [trailingText] — значение перед правой иконкой. [toggled] — пункт-переключатель: включён — золотой, выключен — приглушённый.
 */
@Composable
fun ActionMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: Painter? = null,
    trailingIcon: Painter? = null,
    trailingText: String? = null,
    toggled: Boolean? = null,
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
            val color = when {
                !enabled -> colors.textDisabled
                toggled == true -> colors.accentGold
                toggled == false -> if (hovered) colors.textPrimary else colors.textMuted
                hovered -> colors.accentGold
                else -> colors.textPrimary
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimens.spaceS)) {
                icon?.let { MenuItemIcon(it, color) }
                Text(text, Modifier.weight(1f), style = AppTheme.typography.bodyBold, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
                trailingText?.let { Text(it, style = AppTheme.typography.bodyBold, color = colors.accentGold) }
                trailingIcon?.let { MenuItemIcon(it, color) }
            }
        }
    }
}

@Composable
private fun MenuItemIcon(icon: Painter, color: Color) {
    Image(icon, contentDescription = null, modifier = Modifier.size(AppTheme.dimens.iconSize), colorFilter = ColorFilter.tint(color))
}
