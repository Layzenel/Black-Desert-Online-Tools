package com.layzen.bdotools.feature.settings.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.layzen.bdotools.core.theme.WindowTheme
import com.layzen.bdotools.feature.settings.resources.Res
import com.layzen.bdotools.feature.settings.resources.settings_theme_description
import com.layzen.bdotools.feature.settings.resources.settings_theme_preview_title
import com.layzen.bdotools.feature.settings.resources.settings_theme_title
import com.layzen.bdotools.uikit.components.HoverTooltip
import com.layzen.bdotools.uikit.components.OptionButton
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.WindowThemePreview
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

/** Тем в строке — как в окне настроек игры. */
private const val THEMES_PER_ROW = 3

/** Раздел «Тема»: варианты тем окон по три в строке; наведение на вариант показывает превью окна в этой теме. */
@Composable
internal fun ThemeSettings(selected: WindowTheme, onSelect: (WindowTheme) -> Unit) {
    SectionHeader(stringResource(Res.string.settings_theme_title), description = stringResource(Res.string.settings_theme_description))
    val previewTitle = stringResource(Res.string.settings_theme_preview_title)
    WindowTheme.entries.chunked(THEMES_PER_ROW).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceS)) {
            row.forEach { theme ->
                HoverTooltip(tooltip = { WindowThemePreview(theme, previewTitle) }, modifier = Modifier.weight(1f)) {
                    OptionButton(stringResource(theme.title), selected = theme == selected, onClick = { onSelect(theme) })
                }
            }
            repeat(THEMES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}
