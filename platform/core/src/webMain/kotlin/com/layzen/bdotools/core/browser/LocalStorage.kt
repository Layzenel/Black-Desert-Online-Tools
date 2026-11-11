@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.core.browser

// localStorage бывает закрыт (приватный режим, запрет сайтов): тогда значение просто не запоминается

/** Значение `localStorage` сайта по ключу [key] или `null`, если его нет либо хранилище закрыто. */
internal fun readLocalStorage(key: String): String? =
    js("(() => { try { return globalThis.localStorage.getItem(key); } catch (e) { return null; } })()")

/** Записывает [value] в `localStorage` сайта; при закрытом хранилище ничего не делает. */
internal fun writeLocalStorage(key: String, value: String): Unit =
    js("(() => { try { globalThis.localStorage.setItem(key, value); } catch (e) {} })()")
