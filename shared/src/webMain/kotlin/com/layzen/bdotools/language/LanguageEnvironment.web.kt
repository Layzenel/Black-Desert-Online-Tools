@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.language

// Язык браузера поменять нельзя, поэтому скрипт в index.html подменяет navigator.languages значением
// __customLocale: по нему Compose и выбирает папку ресурсов
private fun setCustomLocale(tag: String): Unit =
    js("{ globalThis.__customLocale = tag; globalThis.document.documentElement.lang = tag; }")

internal actual fun applyPlatformLocale(tag: String) = setCustomLocale(tag)
