package com.layzen.bdotools.core.account

import com.layzen.bdotools.core.folder.GameFolderSettings

/** Выбранный аккаунт в настройках программы рядом с папкой игры. */
internal class DesktopAccountChoice(private val settings: GameFolderSettings) : AccountChoice {
    override fun load(): String? = settings.account

    override fun save(id: String) {
        settings.account = id
    }
}

internal actual fun createAccountChoice(): AccountChoice = DesktopAccountChoice(GameFolderSettings())
