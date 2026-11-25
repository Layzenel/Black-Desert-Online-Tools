package com.layzen.bdotools.core.theme

import com.layzen.bdotools.core.browser.readLocalStorage
import com.layzen.bdotools.core.browser.writeLocalStorage

private const val STORAGE_KEY = "bdo-tools-theme"

/** Выбранная тема в `localStorage` сайта. */
internal class BrowserThemeSettings : ThemeSettings {
    override fun saved(): String? = readLocalStorage(STORAGE_KEY)

    override fun save(id: String) = writeLocalStorage(STORAGE_KEY, id)
}

internal actual fun createThemeSettings(): ThemeSettings = BrowserThemeSettings()
