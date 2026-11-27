package com.layzen.bdotools.feature.screenshoteditor.presentation.state

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotError

/** Сообщение о результате действия внизу экрана. */
sealed interface EditorNotice {
    /** Копия сохранена в [path]. */
    data class Saved(val path: String) : EditorNotice

    /** Копия ушла в загрузки браузера. */
    data object Downloaded : EditorNotice

    /** Картинка в буфере обмена. */
    data object Copied : EditorNotice

    /** Не получилось: [error]. */
    data class Failed(val error: ScreenshotError) : EditorNotice
}
