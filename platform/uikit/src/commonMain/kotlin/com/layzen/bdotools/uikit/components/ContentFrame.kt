package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Подложка содержимого страницы без шапки: графитовое тело игрового окна с чёрной рамкой и золотой кромкой
 * сверху. Отделяет содержимое (сетку плиток и строку действий под ней) от фона страницы.
 * Внутри — колонка с отступом `spaceL` и промежутками `spaceM`.
 */
@Composable
fun ContentFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Column(
        modifier
            .border(dimens.borderWidth, Color.Black)
            .background(colors.backgroundSurface)
            .drawBehind {
                val edge = dimens.accentLineWidth.toPx()
                drawLine(colors.titleBarHighlight, Offset(0f, edge / 2), Offset(size.width, edge / 2), edge)
            }
            .padding(dimens.spaceL),
        verticalArrangement = Arrangement.spacedBy(dimens.spaceM),
        content = content,
    )
}
