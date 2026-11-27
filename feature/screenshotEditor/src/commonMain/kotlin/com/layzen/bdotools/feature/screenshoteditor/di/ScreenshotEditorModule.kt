package com.layzen.bdotools.feature.screenshoteditor.di

import com.layzen.bdotools.composable.overlay.OverlayContribution
import com.layzen.bdotools.feature.screenshoteditor.data.createScreenshotFiles
import com.layzen.bdotools.feature.screenshoteditor.data.createScreenshotGallery
import com.layzen.bdotools.feature.screenshoteditor.data.createScreenshotWatch
import com.layzen.bdotools.feature.screenshoteditor.domain.file.GalleryInteractor
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotInteractor
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.ScreenshotWatchInteractor
import com.layzen.bdotools.feature.screenshoteditor.presentation.ScreenshotEditorViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.alert.ScreenshotAlertViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.gallery.GalleryViewModel
import com.layzen.bdotools.feature.screenshoteditor.presentation.ui.screenshotOverlayContribution
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Редактор скриншотов: файлы, галерея и слежение этой платформы, интеракторы, ViewModel и вклад в оверлей. */
val screenshotEditorModule = module {
    single { createScreenshotFiles() }
    single { createScreenshotGallery() }
    single { createScreenshotWatch() }
    factoryOf(::ScreenshotInteractor)
    factoryOf(::GalleryInteractor)
    factoryOf(::ScreenshotWatchInteractor)
    viewModelOf(::ScreenshotEditorViewModel)
    viewModelOf(::GalleryViewModel)
    viewModelOf(::ScreenshotAlertViewModel)
    single<OverlayContribution>(named("screenshots")) { screenshotOverlayContribution() }
}
