package com.layzen.bdotools.core.account

private const val STORAGE_KEY = "bdo-tools-account"

// localStorage бывает закрыт (приватный режим, запрет сайтов): тогда выбор просто не запоминается
private fun readStorage(key: String): String? =
    js("(() => { try { return globalThis.localStorage.getItem(key); } catch (e) { return null; } })()")

private fun writeStorage(key: String, value: String): Unit =
    js("(() => { try { globalThis.localStorage.setItem(key, value); } catch (e) {} })()")

/** Выбранный аккаунт в `localStorage` сайта. */
internal class BrowserAccountChoice : AccountChoice {
    override fun load(): String? = readStorage(STORAGE_KEY)

    override fun save(id: String) = writeStorage(STORAGE_KEY, id)
}

internal actual fun createAccountChoice(): AccountChoice = BrowserAccountChoice()
