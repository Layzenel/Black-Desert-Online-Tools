package com.layzen.bdotools.feature.screenshoteditor.presentation.alert

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotEntry

/**
 * Уведомление о новом скриншоте поверх игры.
 *
 * @property enabled предлагать ли редактировать новые скриншоты
 * @property latest последний новый скриншот; `null` — уведомления нет
 * @property count сколько скриншотов сделали, пока уведомление висит
 */
data class ScreenshotAlertState(
    val enabled: Boolean = true,
    val latest: ScreenshotEntry? = null,
    val count: Int = 0,
)
