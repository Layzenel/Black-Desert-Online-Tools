package com.layzen.bdotools.feature.screenshoteditor.domain.watch

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import kotlinx.coroutines.flow.Flow

/** Сценарии слежения за новыми скриншотами. */
class ScreenshotWatchInteractor(private val watch: ScreenshotWatch) {
    /** См. [ScreenshotWatch.supported]. */
    val supported: Boolean get() = watch.supported

    /** См. [ScreenshotWatch.alertsEnabled]. */
    var alertsEnabled: Boolean
        get() = watch.alertsEnabled
        set(value) {
            watch.alertsEnabled = value
        }

    /** См. [ScreenshotWatch.newScreenshots]. */
    fun newScreenshots(): Flow<ScreenshotEntry> = watch.newScreenshots()
}
