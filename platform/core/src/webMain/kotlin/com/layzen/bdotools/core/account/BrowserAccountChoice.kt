package com.layzen.bdotools.core.account

import com.layzen.bdotools.core.browser.readLocalStorage
import com.layzen.bdotools.core.browser.writeLocalStorage

private const val STORAGE_KEY = "bdo-tools-account"

/** Выбранный аккаунт в `localStorage` сайта. */
internal class BrowserAccountChoice : AccountChoice {
    override fun load(): String? = readLocalStorage(STORAGE_KEY)

    override fun save(id: String) = writeLocalStorage(STORAGE_KEY, id)
}

internal actual fun createAccountChoice(): AccountChoice = BrowserAccountChoice()
