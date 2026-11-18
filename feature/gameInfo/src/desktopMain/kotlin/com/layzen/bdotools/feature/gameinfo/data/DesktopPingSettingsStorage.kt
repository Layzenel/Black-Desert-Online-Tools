package com.layzen.bdotools.feature.gameinfo.data

import java.util.prefs.Preferences

/** Настройки пинга в `Preferences` пользователя: они про программу, а не про аккаунт игры. */
internal class DesktopPingSettingsStorage : PingSettingsStorage {
    private val preferences: Preferences = Preferences.userRoot().node("bdo-tools/game-info")

    override fun read(): String? = preferences.get(KEY, null)

    override fun write(text: String) {
        preferences.put(KEY, text)
        preferences.flush()
    }

    private companion object {
        const val KEY = "ping"
    }
}

internal actual fun createPingSettingsStorage(): PingSettingsStorage = DesktopPingSettingsStorage()
