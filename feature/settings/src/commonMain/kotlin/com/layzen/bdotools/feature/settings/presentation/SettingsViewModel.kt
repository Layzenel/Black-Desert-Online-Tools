package com.layzen.bdotools.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.layzen.bdotools.feature.settings.domain.keyhint.KeyHintsInteractor
import com.layzen.bdotools.feature.settings.domain.startup.StartupInteractor
import com.layzen.bdotools.feature.settings.domain.startup.StartupOptions
import com.layzen.bdotools.feature.settings.domain.theme.ThemesInteractor
import com.layzen.bdotools.feature.settings.presentation.event.SettingsEvent
import com.layzen.bdotools.feature.settings.presentation.state.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Экран настроек: раздел, настройки запуска программы, тема окон и вид подсказок клавиш. */
class SettingsViewModel(
    private val startup: StartupInteractor,
    private val themes: ThemesInteractor,
    private val keyHints: KeyHintsInteractor,
) : ViewModel() {

    private val mutableState = MutableStateFlow(SettingsState(theme = themes.selected.value, keyHints = keyHints.selected.value))

    /** Состояние экрана. */
    val state: StateFlow<SettingsState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch { themes.selected.collect { theme -> mutableState.update { it.copy(theme = theme) } } }
        viewModelScope.launch { keyHints.selected.collect { hints -> mutableState.update { it.copy(keyHints = hints) } } }
        changeStartup { startup.options() }
    }

    /** Обрабатывает действие пользователя [event]. */
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OpenSection -> mutableState.update { it.copy(section = event.section) }
            is SettingsEvent.SetLaunchAtStartup -> changeStartup { startup.setLaunchAtStartup(event.enabled) }
            is SettingsEvent.SetAlwaysAdmin -> changeStartup { startup.setAlwaysAdmin(event.enabled) }
            is SettingsEvent.SetStartInTray -> changeStartup { startup.setStartInTray(event.enabled) }
            is SettingsEvent.SelectTheme -> themes.select(event.theme)
            is SettingsEvent.SelectKeyHints -> keyHints.select(event.hints)
        }
    }

    private fun changeStartup(action: suspend () -> StartupOptions) {
        if (mutableState.value.startupBusy) return
        mutableState.update { it.copy(startupBusy = true) }
        viewModelScope.launch {
            val options = action()
            mutableState.update { it.copy(startup = options, startupBusy = false) }
        }
    }
}
