package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotFiles
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotGallery
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.ScreenshotWatch

/** Файлы скриншотов этой платформы: диск на Desktop, выбор файла и загрузки в браузере. */
internal expect fun createScreenshotFiles(): ScreenshotFiles

/** Папка `ScreenShot` этой платформы: на Desktop — с диска, в браузере — через File System Access API. */
internal expect fun createScreenshotGallery(): ScreenshotGallery

/** Слежение за новыми скриншотами: есть только на Desktop. */
internal expect fun createScreenshotWatch(): ScreenshotWatch
