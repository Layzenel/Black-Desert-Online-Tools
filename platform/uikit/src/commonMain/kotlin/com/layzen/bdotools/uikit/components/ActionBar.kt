package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Панель действий над рабочей областью страницы: слева инструменты правки, после `Spacer(Modifier.weight(1f))` —
 * работа с файлами, главное действие последним справа. Заменяет футер окна на страницах [ToolPage].
 */
@Composable
fun ActionBar(
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
                drawLine(colors.divider, Offset(0f, size.height), Offset(size.width, size.height), dimens.borderWidth.toPx())
            }
            .padding(horizontal = dimens.spaceM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spaceS),
        content = content,
    )
}
