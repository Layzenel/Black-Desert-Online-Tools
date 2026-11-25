package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Игровое окно: бронзовая шапка, тёмное тело и необязательная нижняя панель с кнопками. Рамка — как у окон игры:
 * тонкая тёмная снаружи и светлая бронзовая внутри, сверху золотая кромка. Основной контейнер каждой тулзы.
 * [titleBarModifier] — для шапки, например перетаскивание окна за неё.
 */
@Composable
fun GameWindow(
    title: String,
    modifier: Modifier = Modifier,
    titleBarModifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    footer: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    val frame = AppTheme.dimens.borderWidth
    Column(
        modifier
            .windowDecoration()
            .border(frame, colors.windowOutline)
            .padding(frame)
            .border(frame, colors.windowFrame)
            .drawWithContent {
                drawContent()
                val width = frame.toPx()
                drawLine(colors.titleBarHighlight, Offset(0f, width / 2), Offset(size.width, width / 2), width)
            }
            .padding(frame)
            .background(colors.backgroundDeep),
    ) {
        WindowTitleBar(title = title, modifier = titleBarModifier, icon = icon, actions = actions)
        Column(content = content)
        if (footer != null) {
            WindowFooter(content = footer)
        }
    }
}
