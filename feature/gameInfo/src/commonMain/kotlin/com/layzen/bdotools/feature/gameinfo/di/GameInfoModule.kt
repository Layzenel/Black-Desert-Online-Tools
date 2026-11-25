package com.layzen.bdotools.feature.gameinfo.di

import com.layzen.bdotools.feature.gameinfo.data.PingSettingsRepositoryImpl
import com.layzen.bdotools.feature.gameinfo.data.createGameFpsSource
import com.layzen.bdotools.feature.gameinfo.data.createGamePingSource
import com.layzen.bdotools.feature.gameinfo.data.createGameScreenSource
import com.layzen.bdotools.feature.gameinfo.data.createPingSettingsStorage
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingInteractor
import com.layzen.bdotools.feature.gameinfo.domain.ping.PingSettingsRepository
import com.layzen.bdotools.feature.gameinfo.presentation.GameInfoViewModel
import com.layzen.bdotools.feature.gameinfo.presentation.ui.gameInfoOverlayContribution
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/** Сведения об игре поверх неё: пинг и FPS — замер, настройки показа, ViewModel и вклад в оверлей. */
val gameInfoModule = module {
    single { createGamePingSource() }
    single { createGameFpsSource() }
    single { createGameScreenSource() }
    single { createPingSettingsStorage() }
    singleOf(::PingSettingsRepositoryImpl) bind PingSettingsRepository::class
    factoryOf(::PingInteractor)
    viewModelOf(::GameInfoViewModel)
    single(named("game-info")) { gameInfoOverlayContribution() }
}
