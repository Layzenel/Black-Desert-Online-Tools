package com.layzen.bdotools.core.language

import com.layzen.bdotools.core.folder.GameFolderSettings
import java.util.Locale

/**
 * Выбранный язык в настройках программы рядом с папкой игры.
 *
 * @property systemLanguage язык ОС; читается при создании, пока приложение не подменило [Locale.getDefault] своим языком
 */
internal class DesktopLanguageSettings(
    private val settings: GameFolderSettings,
    private val systemLanguage: String = Locale.getDefault().language,
) : LanguageSettings {
    override fun saved(): String? = settings.language

    override fun save(tag: String) {
        settings.language = tag
    }

    override fun system(): String = systemLanguage
}

internal actual fun createLanguageSettings(): LanguageSettings = DesktopLanguageSettings(GameFolderSettings())
