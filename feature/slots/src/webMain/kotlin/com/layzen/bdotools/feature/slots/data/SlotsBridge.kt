@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.feature.slots.data

import kotlin.js.JsString
import kotlin.js.Promise
import kotlinx.coroutines.await

// Компилятор Kotlin/JS не разбирает async/await внутри js(), поэтому мост выполняется через eval
private fun evaluate(source: String): Unit = js("(0, eval)(source)")

private fun isFileSystemAccessSupported(): Boolean =
    js("typeof globalThis.showDirectoryPicker === 'function' && typeof globalThis.indexedDB !== 'undefined'")

private fun callBridge(method: String, first: String, second: String): Promise<JsString> =
    js("globalThis.__bdoSlots[method](first, second)")

internal object SlotsBridge {

    val isSupported: Boolean by lazy {
        evaluate(SLOTS_BRIDGE_SOURCE)
        isFileSystemAccessSupported()
    }

    /** Вызывает метод моста и возвращает данные после `OK:` или бросает [SlotsException]. */
    suspend fun call(method: String, first: String = "", second: String = ""): String {
        evaluate(SLOTS_BRIDGE_SOURCE)
        val result = callBridge(method, first, second).await<JsString>().toString()
        if (result.startsWith("OK:")) return result.removePrefix("OK:")
        val code = result.removePrefix("ERR:")
        throw SlotsException(
            error = when (code.substringBefore(':')) {
                "CANCELLED" -> SlotsError.Cancelled
                "NOT_GAME_FOLDER" -> SlotsError.NotGameFolder
                "PERMISSION" -> SlotsError.Permission
                "FILE_CHANGED" -> SlotsError.FileChanged
                else -> SlotsError.Unknown
            },
            details = code,
        )
    }
}
