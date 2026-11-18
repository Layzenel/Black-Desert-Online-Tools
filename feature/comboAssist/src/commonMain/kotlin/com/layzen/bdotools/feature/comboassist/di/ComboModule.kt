package com.layzen.bdotools.feature.comboassist.di

import com.layzen.bdotools.feature.comboassist.data.ComboFilesImpl
import com.layzen.bdotools.feature.comboassist.data.ComboRepositoryImpl
import com.layzen.bdotools.feature.comboassist.data.createDeviceTextFiles
import com.layzen.bdotools.feature.comboassist.data.createComboStorage
import com.layzen.bdotools.feature.comboassist.data.history.createSkillHistorySource
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboFiles
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboInteractor
import com.layzen.bdotools.feature.comboassist.domain.combo.ComboRepository
import com.layzen.bdotools.feature.comboassist.presentation.ComboViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.qualifier.named
import com.layzen.bdotools.feature.comboassist.presentation.ui.overlay.comboOverlayContribution

/** Тулза комбо: хранилище комбо, история умений из игры, интерактор и ViewModel. Справочник навыков — из core. */
val comboModule = module {
    single { createComboStorage() }
    singleOf(::ComboRepositoryImpl) bind ComboRepository::class
    single { createSkillHistorySource() }
    single { createDeviceTextFiles() }
    singleOf(::ComboFilesImpl) bind ComboFiles::class
    factoryOf(::ComboInteractor)
    viewModelOf(::ComboViewModel)
    single(named("combo")) { comboOverlayContribution() }
}
