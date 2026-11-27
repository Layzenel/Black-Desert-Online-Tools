package com.layzen.bdotools.feature.screenshoteditor.domain.file

/** Что пошло не так при работе с файлом скриншота. */
enum class ScreenshotError {
    /** Файл не открылся как картинка. */
    NotImage,

    /** Не удалось записать файл. */
    Save,

    /** Буфер обмена недоступен или занят. */
    Clipboard,
}
