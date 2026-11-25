package com.layzen.bdotools.core.theme

import com.layzen.bdotools.core.folder.GameFolderSettings

/** Выбранная тема в настройках программы рядом с папкой игры. */
internal class DesktopThemeSettings(private val settings: GameFolderSettings) : ThemeSettings {
    override fun saved(): String? = settings.theme

    override fun save(id: String) {
        settings.theme = id
    }
}

internal actual fun createThemeSettings(): ThemeSettings = DesktopThemeSettings(GameFolderSettings())
