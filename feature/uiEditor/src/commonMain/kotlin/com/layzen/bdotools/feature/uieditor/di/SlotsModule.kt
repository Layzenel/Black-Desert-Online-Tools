package com.layzen.bdotools.feature.uieditor.di

import com.layzen.bdotools.feature.uieditor.data.SlotsRepositoryImpl
import com.layzen.bdotools.feature.uieditor.data.SlotsStorage
import com.layzen.bdotools.feature.uieditor.data.createSlotsFiles
import com.layzen.bdotools.feature.uieditor.domain.file.DeviceFiles
import com.layzen.bdotools.feature.uieditor.domain.file.LayoutFilesInteractor
import com.layzen.bdotools.feature.uieditor.domain.game.GameSettingsInteractor
import com.layzen.bdotools.feature.uieditor.domain.game.SlotsRepository
import com.layzen.bdotools.feature.uieditor.domain.preset.PresetsInteractor
import com.layzen.bdotools.feature.uieditor.presentation.SlotsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

/** Koin-модуль фичи: файлы своей платформы, реализация репозитория, интеракторы и ViewModel. */
val slotsModule = module {
    single { createSlotsFiles() } binds arrayOf(SlotsStorage::class, DeviceFiles::class)
    singleOf(::SlotsRepositoryImpl) bind SlotsRepository::class
    factoryOf(::GameSettingsInteractor)
    factoryOf(::PresetsInteractor)
    factoryOf(::LayoutFilesInteractor)
    viewModelOf(::SlotsViewModel)
}
