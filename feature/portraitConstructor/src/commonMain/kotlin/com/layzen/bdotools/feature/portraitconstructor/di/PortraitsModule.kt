package com.layzen.bdotools.feature.portraitconstructor.di

import com.layzen.bdotools.feature.portraitconstructor.data.CharacterMemosRepositoryImpl
import com.layzen.bdotools.feature.portraitconstructor.data.PortraitsRepositoryImpl
import com.layzen.bdotools.feature.portraitconstructor.data.PortraitsStorage
import com.layzen.bdotools.feature.portraitconstructor.data.createGameFiles
import com.layzen.bdotools.feature.portraitconstructor.domain.backup.BackupsInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.favorite.FavoritesInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.CharacterMemosRepository
import com.layzen.bdotools.feature.portraitconstructor.domain.memo.MemosInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.ImageSource
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitsInteractor
import com.layzen.bdotools.feature.portraitconstructor.domain.portrait.PortraitsRepository
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * Koin-модуль фичи: файлы игры (реализация своей платформы, см. [createGameFiles]), реализации репозиториев,
 * интеракторы и ViewModel.
 */
val portraitsModule = module {
    single { createGameFiles() } binds arrayOf(PortraitsStorage::class, ImageSource::class)
    singleOf(::PortraitsRepositoryImpl) bind PortraitsRepository::class
    singleOf(::CharacterMemosRepositoryImpl) bind CharacterMemosRepository::class
    factoryOf(::PortraitsInteractor)
    factoryOf(::BackupsInteractor)
    factoryOf(::FavoritesInteractor)
    factoryOf(::MemosInteractor)
    viewModelOf(::PortraitsViewModel)
}
