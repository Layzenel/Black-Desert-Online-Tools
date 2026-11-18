package com.layzen.bdotools.feature.gameinfo.data

import com.layzen.bdotools.core.browser.readLocalStorage
import com.layzen.bdotools.core.browser.writeLocalStorage

/** Настройки пинга в `localStorage` сайта. */
internal class BrowserPingSettingsStorage : PingSettingsStorage {
    override fun read(): String? = readLocalStorage(KEY)
    override fun write(text: String) = writeLocalStorage(KEY, text)

    private companion object {
        const val KEY = "bdo-tools-ping"
    }
}

internal actual fun createPingSettingsStorage(): PingSettingsStorage = BrowserPingSettingsStorage()
