package com.layzen.bdotools.update.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.uikit.theme.AppShapes
import com.layzen.bdotools.uikit.theme.AppTheme

/** Насколько плотная тёмная подложка под версией: бронза навигации чуть просвечивает. */
private const val PANEL_ALPHA = 0.6f

/** Тёмная плашка на бронзовой навигации: версия и кнопки обновления рядом с ней читаются как одно целое. */
@Composable
internal fun VersionPanel(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier
            .background(AppTheme.colors.backgroundDeep.copy(alpha = PANEL_ALPHA), AppShapes.Small)
            .padding(horizontal = AppTheme.dimens.spaceS, vertical = AppTheme.dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
