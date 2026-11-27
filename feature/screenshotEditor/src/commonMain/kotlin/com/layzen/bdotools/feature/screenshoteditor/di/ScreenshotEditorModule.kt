package com.layzen.bdotools.feature.screenshoteditor.di

import com.layzen.bdotools.feature.screenshoteditor.data.createScreenshotFiles
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotInteractor
import com.layzen.bdotools.feature.screenshoteditor.presentation.ScreenshotEditorViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Редактор скриншотов: файлы этой платформы, интерактор и ViewModel. */
val screenshotEditorModule = module {
    single { createScreenshotFiles() }
    factoryOf(::ScreenshotInteractor)
    viewModelOf(::ScreenshotEditorViewModel)
}
