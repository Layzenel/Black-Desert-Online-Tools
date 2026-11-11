@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.update.presentation

/** Перезагружает страницу, чтобы браузер скачал новую версию сайта. */
internal fun reloadPage(): Unit = js("globalThis.location.reload()")
