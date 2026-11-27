package com.layzen.bdotools.feature.screenshoteditor.data

import com.layzen.bdotools.core.folder.GameFolderSettings
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotFiles
import com.layzen.bdotools.feature.screenshoteditor.domain.file.ScreenshotGallery
import com.layzen.bdotools.feature.screenshoteditor.domain.watch.ScreenshotWatch
import java.util.prefs.Preferences

/** Узел настроек редактора в `Preferences` пользователя. */
private const val PREFERENCES_NODE = "bdo-tools/screenshot-editor"

internal actual fun createScreenshotFiles(): ScreenshotFiles = DesktopScreenshotFiles { GameFolderSettings().folder }

internal actual fun createScreenshotGallery(): ScreenshotGallery =
    DesktopScreenshotGallery({ GameFolderSettings().folder }, { GameFolderSettings().folder = it })

internal actual fun createScreenshotWatch(): ScreenshotWatch =
    DesktopScreenshotWatch({ GameFolderSettings().folder }, Preferences.userRoot().node(PREFERENCES_NODE))
