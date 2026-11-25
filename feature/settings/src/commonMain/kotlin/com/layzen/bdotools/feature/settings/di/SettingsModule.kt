package com.layzen.bdotools.feature.settings.di

import com.layzen.bdotools.feature.settings.data.StartupRepositoryImpl
import com.layzen.bdotools.feature.settings.data.createSystemStartup
import com.layzen.bdotools.feature.settings.domain.startup.StartupInteractor
import com.layzen.bdotools.feature.settings.domain.startup.StartupRepository
import com.layzen.bdotools.feature.settings.domain.theme.ThemesInteractor
import com.layzen.bdotools.feature.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin-модуль экрана настроек. */
val settingsModule = module {
    single<StartupRepository> { StartupRepositoryImpl(createSystemStartup()) }
    factoryOf(::StartupInteractor)
    factoryOf(::ThemesInteractor)
    viewModelOf(::SettingsViewModel)
}
