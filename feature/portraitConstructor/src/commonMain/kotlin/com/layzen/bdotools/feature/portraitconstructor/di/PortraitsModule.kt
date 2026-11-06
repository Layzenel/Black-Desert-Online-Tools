package com.layzen.bdotools.feature.portraitconstructor.di

import com.layzen.bdotools.feature.portraitconstructor.data.CharacterMemosRepository
import com.layzen.bdotools.feature.portraitconstructor.data.ImageSource
import com.layzen.bdotools.feature.portraitconstructor.data.PortraitsRepository
import com.layzen.bdotools.feature.portraitconstructor.data.PortraitsStorage
import com.layzen.bdotools.feature.portraitconstructor.data.createGameFiles
import com.layzen.bdotools.feature.portraitconstructor.presentation.PortraitsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.binds
import org.koin.dsl.module

/** Koin-модуль фичи: файлы игры (реализация своей платформы, см. [createGameFiles]), репозиторий и ViewModel. */
val portraitsModule = module {
    single { createGameFiles() } binds arrayOf(PortraitsStorage::class, ImageSource::class)
    singleOf(::PortraitsRepository)
    singleOf(::CharacterMemosRepository)
    viewModelOf(::PortraitsViewModel)
}
