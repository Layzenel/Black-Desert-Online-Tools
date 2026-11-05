package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Страница тулзы без шапки окна: меню слева во всю высоту и содержимое справа на всё оставшееся место.
 * Меню [sidebar] прокручивается само, а [sidebarFooter] прижат к низу меню и не прокручивается — туда ставятся
 * главные действия, которые всегда должны быть под рукой. Содержимое [content] прокручивает себя само, если нужно.
 * Подходит и для большого модального окна: тогда страница лежит внутри [ModalWindow].
 */
@Composable
fun ToolPage(
    sidebar: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sidebarWidth: Dp = AppTheme.dimens.sidebarWidth,
    sidebarFooter: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Row(modifier.background(AppTheme.colors.backgroundDeep)) {
        Sidebar(sidebar, sidebarFooter, Modifier.width(sidebarWidth).fillMaxHeight())
        Box(Modifier.weight(1f).fillMaxHeight(), content = content)
    }
}

@Composable
private fun Sidebar(
    sidebar: @Composable ColumnScope.() -> Unit,
    footer: (@Composable ColumnScope.() -> Unit)?,
    modifier: Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val scroll = rememberScrollState()
    Column(
        modifier
            .background(colors.backgroundSurface)
            .drawBehind {
                drawLine(colors.divider, Offset(size.width, 0f), Offset(size.width, size.height), dimens.borderWidth.toPx())
            },
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScrollbar(scroll)
                .verticalScroll(scroll)
                .padding(dimens.spaceL),
            verticalArrangement = Arrangement.spacedBy(dimens.spaceM),
            content = sidebar,
        )
        if (footer != null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.backgroundRaised)
                    .drawBehind { drawLine(colors.divider, Offset.Zero, Offset(size.width, 0f), dimens.borderWidth.toPx()) }
                    .padding(dimens.spaceM),
                verticalArrangement = Arrangement.spacedBy(dimens.spaceS),
                content = footer,
            )
        }
    }
}
