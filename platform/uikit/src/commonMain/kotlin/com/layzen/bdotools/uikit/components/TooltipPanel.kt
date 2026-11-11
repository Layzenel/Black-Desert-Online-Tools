package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppTheme

/** Подложка подсказки: панель с рамкой, как у тултипов игры. Её рисуют [HoverTooltip] и `aside` у [ModalWindow]. */
@Composable
fun TooltipPanel(content: @Composable () -> Unit) {
    Box(
        Modifier
            .background(AppTheme.colors.backgroundRaised)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border)
            .padding(horizontal = AppTheme.dimens.spaceS, vertical = AppTheme.dimens.spaceXs),
    ) { content() }
}
