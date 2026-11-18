package com.layzen.bdotools.feature.gameinfo.data

/** Где лежат настройки пинга: строка текста. Ошибки не бросаются: без хранилища настройки просто не запомнятся. */
interface PingSettingsStorage {
    /** Сохранённый текст или `null`. */
    fun read(): String?

    /** Сохраняет [text]. */
    fun write(text: String)
}

/** Хранилище своей платформы: в настольной версии — `Preferences` пользователя, на сайте — `localStorage`. */
internal expect fun createPingSettingsStorage(): PingSettingsStorage
