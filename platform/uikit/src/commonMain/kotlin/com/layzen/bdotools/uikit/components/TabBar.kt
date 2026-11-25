package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Вкладки, как «Продажа / Покупка» на рынке игры: ряд у левого края, ширина вкладки — по подписи, но не уже
 * `tabMinWidth`; на всю ширину не растягиваются. Невыбранная — тёмная с серой подписью, под курсором светлее
 * с белой подписью, выбранная — светлая графитовая с белой подписью.
 * [fill] — вкладки делят ширину ряда поровну (узкое окно, например меню оверлея); ряду тогда нужна ширина.
 */
@Composable
fun <T> TabBar(
    tabs: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    fill: Boolean = false,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
        tabs.forEach { tab -> TabItem(label(tab), tab == selected, if (fill) Modifier.weight(1f) else Modifier) { onSelect(tab) } }
    }
}

@Composable
private fun TabItem(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val background = when {
        selected -> colors.tabSelected
        hovered -> colors.tabHover
        else -> colors.tab
    }
    Box(
        modifier
            .widthIn(min = dimens.tabMinWidth)
            .height(dimens.tabHeight)
            .pointerHoverIcon(if (selected) PointerIcon.Default else PointerIcon.Hand)
            .clickable(interaction, indication = null, enabled = !selected, role = Role.Tab, onClick = onClick)
            .background(background, AppShapes.Small)
            .border(dimens.borderWidth, if (selected || hovered) colors.tabSelectedBorder else colors.tabBorder, AppShapes.Small)
            .padding(horizontal = dimens.spaceL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = AppTheme.typography.button,
            color = if (selected || hovered) colors.textButton else colors.textTab,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
