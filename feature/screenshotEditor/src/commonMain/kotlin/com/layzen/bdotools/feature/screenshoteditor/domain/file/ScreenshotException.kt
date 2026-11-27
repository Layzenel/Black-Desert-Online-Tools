package com.layzen.bdotools.feature.screenshoteditor.domain.file

/** Ошибка [error] при работе с файлом скриншота; [details] — техническое описание для журнала. */
class ScreenshotException(val error: ScreenshotError, details: String? = null) : Exception(details ?: error.name)
