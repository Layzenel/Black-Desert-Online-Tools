package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/** Вертикальный разделитель между группами кнопок панели инструментов ([ActionBar]). */
@Composable
fun ToolbarDivider(modifier: Modifier = Modifier) {
    Box(modifier.width(AppTheme.dimens.borderWidth).height(AppTheme.dimens.buttonHeight).background(AppTheme.colors.divider))
}
