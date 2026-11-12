package com.layzen.bdotools.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Подложка подсказки, как у тултипов игры: тёмное тело в двойной рамке — внешняя бронзовая, внутренняя тонкая
 * графитовая с зазором, — и небольшая тень, как у меню: так подсказка отделяется от того, над чем всплыла.
 * Её рисуют [HoverTooltip] и `aside` у [ModalWindow]; крупные блоки внутри — [TooltipSection].
 */
@Composable
fun TooltipPanel(content: @Composable () -> Unit) {
    val dimens = AppTheme.dimens
    val colors = AppTheme.colors
    Box(
        Modifier
            .shadow(dimens.menuShadow)
            .background(colors.backgroundDeep)
            .border(dimens.borderWidth, colors.borderSelected)
            .padding(dimens.tooltipFrameGap)
            .border(dimens.borderWidth, colors.border)
            .padding(horizontal = dimens.spaceS, vertical = dimens.spaceXs),
    ) { content() }
}
