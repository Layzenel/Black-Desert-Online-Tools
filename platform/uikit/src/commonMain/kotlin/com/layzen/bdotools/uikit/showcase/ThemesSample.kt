package com.layzen.bdotools.uikit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.theme.WindowTheme
import com.layzen.bdotools.uikit.components.WindowThemePreview
import com.layzen.bdotools.uikit.resources.Res
import com.layzen.bdotools.uikit.resources.showcase_theme_window
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Все темы окон рядом: шапка, украшение над ней и кнопка закрытия каждой ([WindowThemePreview]). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemesSample(modifier: Modifier = Modifier) {
    val title = stringResource(Res.string.showcase_theme_window)
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceL)) {
        WindowTheme.entries.forEach { WindowThemePreview(it, title) }
    }
}
