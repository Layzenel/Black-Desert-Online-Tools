package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
