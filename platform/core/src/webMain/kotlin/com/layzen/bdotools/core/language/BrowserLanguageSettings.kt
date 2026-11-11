@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.core.language

import com.layzen.bdotools.core.browser.readLocalStorage
import com.layzen.bdotools.core.browser.writeLocalStorage

private const val STORAGE_KEY = "bdo-tools-language"

private fun browserLanguage(): String = js("globalThis.navigator.language || ''")

/** Выбранный язык в `localStorage` сайта; язык системы — язык браузера. */
internal class BrowserLanguageSettings : LanguageSettings {
    override fun saved(): String? = readLocalStorage(STORAGE_KEY)

    override fun save(tag: String) = writeLocalStorage(STORAGE_KEY, tag)

    override fun system(): String = browserLanguage()
}

internal actual fun createLanguageSettings(): LanguageSettings = BrowserLanguageSettings()
