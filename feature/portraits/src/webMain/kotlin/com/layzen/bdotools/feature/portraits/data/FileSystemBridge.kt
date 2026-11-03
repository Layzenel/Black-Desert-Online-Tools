@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.feature.portraits.data

import kotlin.js.JsString
import kotlin.js.Promise
import kotlinx.coroutines.await

// Компилятор Kotlin/JS не разбирает async/await внутри js(), поэтому мост выполняется через eval
private fun evaluate(source: String): Unit = js("(0, eval)(source)")

private fun installBridge() = evaluate(BRIDGE_SOURCE)

private fun isFileSystemAccessSupported(): Boolean =
    js("typeof globalThis.showDirectoryPicker === 'function' && typeof globalThis.indexedDB !== 'undefined'")

private fun callBridge(method: String, first: String, second: String): Promise<JsString> =
    js("globalThis.__bdoPortraits[method](first, second)")

internal object FileSystemBridge {
    const val ORDER_SEPARATOR = "\n<!--bdo-split-->\n"

    val isSupported: Boolean by lazy {
        installBridge()
        isFileSystemAccessSupported()
    }

    /** Вызывает метод моста и возвращает данные после `OK:` или бросает [PortraitsException]. */
    suspend fun call(method: String, first: String = "", second: String = ""): String {
        installBridge()
        val result = callBridge(method, first, second).await<JsString>().toString()
        if (result.startsWith("OK:")) return result.removePrefix("OK:")
        val code = result.removePrefix("ERR:")
        throw PortraitsException(
            error = when (code.substringBefore(':')) {
                "CANCELLED" -> PortraitsError.Cancelled
                "NOT_GAME_FOLDER" -> PortraitsError.NotGameFolder
                "PERMISSION" -> PortraitsError.Permission
                "BLOCKED" -> PortraitsError.Blocked
                "NETWORK" -> PortraitsError.Network
                "NOT_IMAGE" -> PortraitsError.NotImage
                else -> PortraitsError.Unknown
            },
            details = code,
        )
    }
}
