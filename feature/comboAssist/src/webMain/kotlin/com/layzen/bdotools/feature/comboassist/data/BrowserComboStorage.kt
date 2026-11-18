package com.layzen.bdotools.feature.comboassist.data

import com.layzen.bdotools.core.browser.readLocalStorage
import com.layzen.bdotools.core.browser.writeLocalStorage

private const val STORAGE_KEY = "bdo-tools-combos"

/** Комбо в `localStorage` сайта: редактор работает без доступа к папке игры. */
internal class BrowserComboStorage : ComboStorage {
    override suspend fun read(): String? = readLocalStorage(STORAGE_KEY)

    override suspend fun write(text: String): Boolean {
        writeLocalStorage(STORAGE_KEY, text)
        return readLocalStorage(STORAGE_KEY) == text
    }
}

internal actual fun createComboStorage(): ComboStorage = BrowserComboStorage()
