package com.layzen.bdotools.core.theme

import org.koin.dsl.module

/** Koin-модуль темы окон: один [AppThemes] на всё приложение. */
val themeModule = module {
    single { AppThemes(createThemeSettings()) }
}
