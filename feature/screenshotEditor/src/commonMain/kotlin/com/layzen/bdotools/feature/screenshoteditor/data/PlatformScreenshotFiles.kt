package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotFiles

/** Файлы скриншотов этой платформы: диск на Desktop, выбор файла и загрузки в браузере. */
internal expect fun createScreenshotFiles(): ScreenshotFiles
