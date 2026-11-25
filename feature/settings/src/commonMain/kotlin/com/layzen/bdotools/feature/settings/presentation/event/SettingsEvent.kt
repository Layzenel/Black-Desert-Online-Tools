package com.layzen.bdotools.feature.settings.presentation.event

import com.layzen.bdotools.core.theme.WindowTheme
import com.layzen.bdotools.feature.settings.presentation.state.SettingsSection

/** Действия пользователя на экране настроек. */
sealed interface SettingsEvent {
    data class OpenSection(val section: SettingsSection) : SettingsEvent
    data class SetLaunchAtStartup(val enabled: Boolean) : SettingsEvent
    data class SetAlwaysAdmin(val enabled: Boolean) : SettingsEvent
    data class SelectTheme(val theme: WindowTheme) : SettingsEvent
}
