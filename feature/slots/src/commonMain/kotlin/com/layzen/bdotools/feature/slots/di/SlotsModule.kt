package com.layzen.bdotools.feature.slots.di

import com.layzen.bdotools.feature.slots.data.DeviceFiles
import com.layzen.bdotools.feature.slots.data.SlotsRepository
import com.layzen.bdotools.feature.slots.data.SlotsStorage
import com.layzen.bdotools.feature.slots.data.createSlotsFiles
import com.layzen.bdotools.feature.slots.presentation.SlotsViewModel
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
