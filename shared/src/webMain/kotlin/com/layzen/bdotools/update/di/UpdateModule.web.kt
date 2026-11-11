package com.layzen.bdotools.update.di

import com.layzen.bdotools.update.data.SiteVersionRepositoryImpl
import com.layzen.bdotools.update.domain.SiteUpdateInteractor
import com.layzen.bdotools.update.domain.SiteVersionRepository
import com.layzen.bdotools.update.presentation.WebUpdateViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val updateModule: Module = module {
    singleOf(::SiteVersionRepositoryImpl) bind SiteVersionRepository::class
    factoryOf(::SiteUpdateInteractor)
    viewModelOf(::WebUpdateViewModel)
}
