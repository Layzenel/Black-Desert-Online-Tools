package com.layzen.bdotools.feature.screenshoteditor.domain.watch

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry
import kotlinx.coroutines.flow.Flow

/** Слежение за папкой `ScreenShot` и предложение отредактировать новый скриншот поверх игры. */
interface ScreenshotWatch {
    /** Слежение есть на этой платформе: только в настольной версии. */
    val supported: Boolean

    /** Предлагать ли редактировать новые скриншоты; помнится между запусками. */
    var alertsEnabled: Boolean

    /** Скриншоты, которые игра только что дописала, по одному на файл. Пока папки нет — ждёт её появления. */
    fun newScreenshots(): Flow<ScreenshotEntry>
}
