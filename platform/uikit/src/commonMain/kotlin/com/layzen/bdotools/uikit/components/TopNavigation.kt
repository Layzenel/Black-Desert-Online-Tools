package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Верхняя навигация сайта: бронзовая полоса в стиле шапки окна, название слева и вкладки.
 * Выбранная вкладка — тёмная подложка, золотой текст и золотая черта снизу.
 * На узком экране вкладки прокручиваются по горизонтали.
 * [selected] равен `null`, когда открыт экран вне вкладок.
 */
@Composable
fun <T> TopNavigation(
    tabs: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        modifier
            .fillMaxWidth()
            .height(dimens.topNavigationHeight)
            .background(Brush.verticalGradient(listOf(colors.titleBar, colors.titleBarDark)))
            .drawBehind {
                val stroke = dimens.borderWidth.toPx()
                drawLine(colors.titleBarHighlight, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
                drawLine(Color.Black, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
            }
            .padding(start = dimens.spaceL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceXl),
    ) {
        if (title != null) {
            Text(title, style = AppTheme.typography.windowTitle, color = colors.textPrimary, maxLines = 1)
        }
        Row(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(rememberScrollState()),
        ) {
            tabs.forEach { tab ->
                TopNavigationTab(
                    text = label(tab),
                    selected = tab == selected,
                    onClick = { onSelect(tab) },
                )
            }
        }
    }
}

@Composable
private fun TopNavigationTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        Modifier
            .fillMaxHeight()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interaction, indication = null, role = Role.Tab, onClick = onClick)
            .background(
                when {
                    selected -> colors.backgroundDeep.copy(alpha = 0.55f)
                    hovered -> colors.backgroundDeep.copy(alpha = 0.25f)
                    else -> Color.Transparent
                },
            )
            .drawBehind {
                if (selected) {
                    val stroke = dimens.accentLineWidth.toPx()
                    drawLine(
                        colors.accentGold,
                        Offset(0f, size.height - stroke / 2 - dimens.borderWidth.toPx()),
                        Offset(size.width, size.height - stroke / 2 - dimens.borderWidth.toPx()),
                        stroke,
                    )
                }
            }
            .padding(horizontal = dimens.spaceL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.menuItem,
            color = when {
                selected -> colors.accentGold
                hovered -> colors.textPrimary
                else -> colors.textBeige
            },
            maxLines = 1,
        )
    }
}
