package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Блок внутри большой подсказки [TooltipPanel], как разделы тултипа навыка в игре: графитовая плашка в тонкой
 * рамке на всю ширину, сверху необязательный бежевый заголовок [title].
 */
@Composable
fun TooltipSection(modifier: Modifier = Modifier, title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .background(AppTheme.colors.backgroundSurface)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border)
            .padding(AppTheme.dimens.spaceS),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs),
    ) {
        if (title != null) Text(title, style = AppTheme.typography.bodyBold, color = AppTheme.colors.textBeige)
        content()
    }
}
