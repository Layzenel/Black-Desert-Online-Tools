package com.layzen.bdotools.core.keyhint

import com.layzen.bdotools.core.folder.GameFolderSettings

/** Вид подсказок клавиш в настройках программы рядом с папкой игры. */
internal class DesktopKeyHintSettings(private val settings: GameFolderSettings) : KeyHintSettings {
    override fun saved(): String? = settings.keyHints

    override fun save(encoded: String) {
        settings.keyHints = encoded
    }
}

internal actual fun createKeyHintSettings(): KeyHintSettings = DesktopKeyHintSettings(GameFolderSettings())
