package com.layzen.bdotools.feature.settings.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.layzen.bdotools.feature.settings.presentation.event.SettingsEvent
import com.layzen.bdotools.feature.settings.presentation.state.SettingsSection
import com.layzen.bdotools.feature.settings.presentation.state.SettingsState
import com.layzen.bdotools.uikit.components.ContentFrame
import com.layzen.bdotools.uikit.theme.AppTheme

/**
 * Настройки программы: по центру страницы на подложке [ContentFrame] шириной как портреты — разделы в меню слева,
 * настройки открытого раздела справа.
 */
@Composable
fun SettingsScreen(state: SettingsState, onEvent: (SettingsEvent) -> Unit) {
    val dimens = AppTheme.dimens
    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(dimens.spaceXl), contentAlignment = Alignment.TopCenter) {
        ContentFrame(Modifier.widthIn(max = dimens.frameMaxWidth)) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spaceL)) {
                SettingsMenu(state.section, { onEvent(SettingsEvent.OpenSection(it)) }, Modifier.width(dimens.sidebarWidth))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dimens.spaceM)) {
                    when (state.section) {
                        SettingsSection.General -> GeneralSettings(state, onEvent)
                        SettingsSection.Theme -> ThemeSettings(state.theme) { onEvent(SettingsEvent.SelectTheme(it)) }
                        SettingsSection.Overlay -> OverlaySettings()
                    }
                }
            }
        }
    }
}
