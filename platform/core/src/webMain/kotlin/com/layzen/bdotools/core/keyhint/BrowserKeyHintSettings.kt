package com.layzen.bdotools.core.keyhint

import com.layzen.bdotools.core.browser.readLocalStorage
import com.layzen.bdotools.core.browser.writeLocalStorage

private const val STORAGE_KEY = "bdo-tools-key-hints"

/** Вид подсказок клавиш в `localStorage` сайта. */
internal class BrowserKeyHintSettings : KeyHintSettings {
    override fun saved(): String? = readLocalStorage(STORAGE_KEY)

    override fun save(encoded: String) = writeLocalStorage(STORAGE_KEY, encoded)
}

internal actual fun createKeyHintSettings(): KeyHintSettings = BrowserKeyHintSettings()
