package com.layzen.bdotools.feature.portraits.di

import com.layzen.bdotools.feature.portraits.data.ImageSource
import com.layzen.bdotools.feature.portraits.data.PortraitsRepository
import com.layzen.bdotools.feature.portraits.data.PortraitsStorage
import com.layzen.bdotools.feature.portraits.data.createGameFiles
import com.layzen.bdotools.feature.portraits.presentation.PortraitsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.binds
import org.koin.dsl.module

/** Koin-модуль фичи: файлы игры (реализация своей платформы, см. [createGameFiles]), репозиторий и ViewModel. */
val portraitsModule = module {
    single { createGameFiles() } binds arrayOf(PortraitsStorage::class, ImageSource::class)
    singleOf(::PortraitsRepository)
    viewModelOf(::PortraitsViewModel)
}
