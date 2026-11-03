package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import com.layzen.bdotools.uikit.theme.AppTheme

/** Вложенный пункт [NavGroup]: активный — золотой, остальные — серые. */
@Composable
fun NavItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Text(
        text = text,
        style = AppTheme.typography.sectionTitle,
        color = when {
            selected -> colors.accentGold
            hovered -> colors.textBeige
            else -> colors.textDisabled
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppTheme.dimens.navItemHeight)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Tab, onClick = onClick)
            .padding(start = AppTheme.dimens.spaceL, top = AppTheme.dimens.spaceS),
    )
}
