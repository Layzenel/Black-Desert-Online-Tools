package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Игровое окно: бронзовая шапка с золотой кромкой, тёмное тело и
 * необязательная нижняя панель с кнопками. Основной контейнер каждой тулзы.
 */
@Composable
fun GameWindow(
    title: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    footer: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier
            .border(AppTheme.dimens.borderWidth, Color.Black)
            .background(colors.backgroundDeep),
    ) {
        WindowTitleBar(title = title, icon = icon, actions = actions)
        Column(content = content)
        if (footer != null) {
            WindowFooter(content = footer)
        }
    }
}

@Composable
fun WindowTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        modifier
            .fillMaxWidth()
            .height(dimens.titleBarHeight)
            .background(Brush.verticalGradient(listOf(colors.titleBar, colors.titleBarDark)))
            .drawBehind {
                val stroke = dimens.borderWidth.toPx()
                drawLine(colors.titleBarHighlight, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
                drawLine(Color.Black, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
            }
            .padding(horizontal = dimens.spaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
    ) {
        if (icon != null) icon()
        Text(
            text = title,
            style = AppTheme.typography.windowTitle,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/** Нижняя панель окна: кнопки «Применить / Назад / OK», подсказки по управлению. */
@Composable
fun WindowFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        modifier
            .fillMaxWidth()
            .height(dimens.footerHeight)
            .background(colors.backgroundRaised)
            .drawBehind {
                drawLine(colors.divider, Offset.Zero, Offset(size.width, 0f), dimens.borderWidth.toPx())
            }
            .padding(horizontal = dimens.spaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS, Alignment.End),
        content = content,
    )
}

/** Панель-подложка внутри окна (полоса инструментов, блок контента). */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.background(AppTheme.colors.backgroundRaised)) { content() }
}
