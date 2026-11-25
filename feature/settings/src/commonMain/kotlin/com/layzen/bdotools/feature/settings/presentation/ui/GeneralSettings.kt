package com.layzen.bdotools.feature.settings.presentation.ui

import androidx.compose.runtime.Composable
import com.layzen.bdotools.feature.settings.presentation.event.SettingsEvent
import com.layzen.bdotools.feature.settings.presentation.state.SettingsState
import com.layzen.bdotools.feature.settings.resources.Res
import com.layzen.bdotools.feature.settings.resources.settings_always_admin
import com.layzen.bdotools.feature.settings.resources.settings_always_admin_description
import com.layzen.bdotools.feature.settings.resources.settings_launch_at_startup
import com.layzen.bdotools.feature.settings.resources.settings_launch_at_startup_description
import com.layzen.bdotools.feature.settings.resources.settings_startup_desktop_only
import com.layzen.bdotools.feature.settings.resources.settings_startup_title
import com.layzen.bdotools.uikit.components.SectionHeader
import com.layzen.bdotools.uikit.components.ToggleSwitch
import org.jetbrains.compose.resources.stringResource

/** Раздел «Основное»: автозапуск и запуск от имени администратора (только установленная настольная версия). */
@Composable
internal fun GeneralSettings(state: SettingsState, onEvent: (SettingsEvent) -> Unit) {
    val startup = state.startup
    val enabled = startup.supported && !state.startupBusy
    SectionHeader(
        stringResource(Res.string.settings_startup_title),
        warning = if (startup.supported) null else stringResource(Res.string.settings_startup_desktop_only),
    )
    ToggleSwitch(
        checked = startup.launchAtStartup,
        onCheckedChange = { onEvent(SettingsEvent.SetLaunchAtStartup(it)) },
        label = stringResource(Res.string.settings_launch_at_startup),
        description = stringResource(Res.string.settings_launch_at_startup_description),
        enabled = enabled,
    )
    ToggleSwitch(
        checked = startup.alwaysAdmin,
        onCheckedChange = { onEvent(SettingsEvent.SetAlwaysAdmin(it)) },
        label = stringResource(Res.string.settings_always_admin),
        description = stringResource(Res.string.settings_always_admin_description),
        enabled = enabled,
    )
}
