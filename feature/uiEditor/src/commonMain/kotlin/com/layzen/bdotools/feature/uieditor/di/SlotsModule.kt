package com.layzen.bdotools.feature.uieditor.di

import com.layzen.bdotools.feature.uieditor.data.DeviceFiles
import com.layzen.bdotools.feature.uieditor.data.SlotsRepository
import com.layzen.bdotools.feature.uieditor.data.SlotsStorage
import com.layzen.bdotools.feature.uieditor.data.createSlotsFiles
import com.layzen.bdotools.feature.uieditor.presentation.SlotsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.binds
import org.koin.dsl.module

/** Koin-модуль фичи: файлы через браузер, репозиторий и ViewModel. */
val slotsModule = module {
    single { createSlotsFiles() } binds arrayOf(SlotsStorage::class, DeviceFiles::class)
    singleOf(::SlotsRepository)
    viewModelOf(::SlotsViewModel)
}
