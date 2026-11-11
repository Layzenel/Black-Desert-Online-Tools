package com.layzen.bdotools.update.di

import com.layzen.bdotools.update.data.InstalledApp
import com.layzen.bdotools.update.data.UpdateRepositoryImpl
import com.layzen.bdotools.update.data.UpdateSource
import com.layzen.bdotools.update.domain.UpdateInteractor
import com.layzen.bdotools.update.domain.UpdateRepository
import com.layzen.bdotools.update.presentation.UpdateViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal actual val updateModule: Module = module {
    single<UpdateRepository> { UpdateRepositoryImpl(InstalledApp.current(), UpdateSource()) }
    factoryOf(::UpdateInteractor)
    viewModelOf(::UpdateViewModel)
}
