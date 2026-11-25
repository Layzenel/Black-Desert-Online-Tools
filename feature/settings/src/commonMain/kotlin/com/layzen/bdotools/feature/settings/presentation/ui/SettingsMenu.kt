package com.layzen.bdotools.feature.settings.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.layzen.bdotools.feature.settings.presentation.state.SettingsSection
import com.layzen.bdotools.uikit.components.MenuItem
import com.layzen.bdotools.uikit.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Разделы настроек столбиком, как меню настроек игры: иконка и подпись, открытый раздел выделен. */
@Composable
internal fun SettingsMenu(selected: SettingsSection, onSelect: (SettingsSection) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXxs)) {
        SettingsSection.entries.forEach { section ->
            MenuItem(
                stringResource(section.title),
                onClick = { onSelect(section) },
                icon = {
                    // цвет иконки — цвет подписи пункта, его задаёт MenuItem
                    Image(painterResource(section.icon), null, Modifier.size(AppTheme.dimens.iconSize), colorFilter = ColorFilter.tint(LocalContentColor.current))
                },
                selected = section == selected,
            )
        }
    }
}
