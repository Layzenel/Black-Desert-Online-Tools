package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotFiles
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotGallery
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.ScreenshotWatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal actual fun createScreenshotFiles(): ScreenshotFiles = BrowserScreenshotFiles()

internal actual fun createScreenshotGallery(): ScreenshotGallery = BrowserScreenshotGallery()

internal actual fun createScreenshotWatch(): ScreenshotWatch = NoScreenshotWatch

/** На сайте поверх игры ничего не показывается, поэтому и следить за папкой незачем. */
private object NoScreenshotWatch : ScreenshotWatch {
    override val supported = false
    override var alertsEnabled = false
    override fun newScreenshots(): Flow<ScreenshotEntry> = emptyFlow()
}
