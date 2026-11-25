package com.layzen.bdotools.core.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Тема окон, общая для всего приложения: выбранная раньше или [WindowTheme.Default]. */
class AppThemes(private val settings: ThemeSettings) {

    private val mutableSelected = MutableStateFlow(WindowTheme.fromId(settings.saved()) ?: WindowTheme.Default)

    /** Текущая тема; меняется только через [select]. */
    val selected: StateFlow<WindowTheme> = mutableSelected.asStateFlow()

    /** Переключает окна на тему [theme] и запоминает выбор. */
    fun select(theme: WindowTheme) {
        settings.save(theme.id)
        mutableSelected.value = theme
    }
}
