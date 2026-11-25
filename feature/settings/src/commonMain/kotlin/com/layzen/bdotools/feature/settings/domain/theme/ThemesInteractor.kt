package com.layzen.bdotools.feature.settings.domain.theme

import com.layzen.bdotools.core.theme.AppThemes
import com.layzen.bdotools.core.theme.WindowTheme
import kotlinx.coroutines.flow.StateFlow

/** Выбор темы окон для всего приложения. */
class ThemesInteractor(private val themes: AppThemes) {
    /** Текущая тема окон. */
    val selected: StateFlow<WindowTheme> get() = themes.selected

    /** Переключает окна на тему [theme] и запоминает выбор. */
    fun select(theme: WindowTheme) = themes.select(theme)
}
