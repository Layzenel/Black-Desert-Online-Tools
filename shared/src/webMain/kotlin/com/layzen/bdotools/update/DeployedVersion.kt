@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.update

import kotlin.js.Promise
import kotlinx.coroutines.await

/** Файл с номером выложенной версии сайта: его кладёт рядом с `index.html` `.github/workflows/deploy.yml`. */
private const val VERSION_URL = "version.txt"

// no-store: иначе браузер и GitHub Pages отдают старый файл из кэша. Ошибка сети — пустая строка
private fun fetchText(url: String): Promise<JsString> =
    js("fetch(url, { cache: 'no-store' }).then(function (r) { return r.ok ? r.text() : '' }).catch(function () { return '' })")

/** Номер версии, выложенной на сайт сейчас, или `null`, если файл не скачался. */
internal suspend fun fetchDeployedVersion(): String? =
    fetchText(VERSION_URL).await<JsString>().toString().trim().ifEmpty { null }

/** Перезагружает страницу, чтобы браузер скачал новую версию сайта. */
internal fun reloadPage(): Unit = js("globalThis.location.reload()")
