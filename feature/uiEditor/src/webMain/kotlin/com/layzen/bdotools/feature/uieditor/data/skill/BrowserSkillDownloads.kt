@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalEncodingApi::class)

package com.layzen.bdotools.feature.uieditor.data.skill

import kotlinx.coroutines.await
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.Promise

// Ошибка сети или ответ не 200 — пустая строка
private fun fetchText(url: String): Promise<JsString> =
    js("fetch(url).then(function (r) { return r.ok ? r.text() : '' }).catch(function () { return '' })")

// Байты через base64: так их проще всего передать и в js, и в wasm. Ошибка — пустая строка
private fun fetchBase64(url: String): Promise<JsString> = js(
    """fetch(url)
        .then(function (r) { return r.ok ? r.arrayBuffer() : null })
        .then(function (buffer) {
            if (!buffer) return '';
            var bytes = new Uint8Array(buffer), chunk = 0x8000, binary = '';
            for (var i = 0; i < bytes.length; i += chunk) binary += String.fromCharCode.apply(null, bytes.subarray(i, i + chunk));
            return btoa(binary);
        })
        .catch(function () { return '' })""",
)

/** [SkillDownloads] на `fetch` браузера; сайт данных отдаёт `Access-Control-Allow-Origin: *`. */
internal class BrowserSkillDownloads : SkillDownloads {

    override suspend fun text(url: String): String? = fetchText(url).await<JsString>().toString().ifEmpty { null }

    override suspend fun bytes(url: String): ByteArray? =
        fetchBase64(url).await<JsString>().toString().ifEmpty { null }?.let { Base64.decode(it) }
}

internal actual fun createSkillDownloads(): SkillDownloads = BrowserSkillDownloads()
