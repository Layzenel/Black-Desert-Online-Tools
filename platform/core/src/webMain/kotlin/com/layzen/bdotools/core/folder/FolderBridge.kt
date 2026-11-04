@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.core.folder

import kotlin.js.JsString
import kotlin.js.Promise
import kotlinx.coroutines.await

// Компилятор Kotlin/JS не разбирает async/await внутри js(), поэтому мост выполняется через eval
private fun evaluate(source: String): Unit = js("(0, eval)(source)")

private fun isFileSystemAccessSupported(): Boolean =
    js("typeof globalThis.showDirectoryPicker === 'function' && typeof globalThis.indexedDB !== 'undefined'")

private fun callBridge(name: String, method: String, first: String, second: String): Promise<JsString> =
    js("globalThis[name][method](first, second)")

/**
 * Мост фичи к папке `Документы\Black Desert` через File System Access API (Chrome, Edge, Opera, Яндекс.Браузер).
 * Выбор папки, разрешение и её хранение общие для всех фич (см. [FOLDER_BRIDGE_SOURCE]),
 * а работу с файлами фича описывает сама в [script].
 *
 * @param name имя объекта моста в `globalThis`, своё у каждой фичи.
 * @param requiredFolder папка внутри `Black Desert`, по которой [selectFolder] узнаёт папку игры.
 * @param script JS-выражение `helpers => ({ метод(a, b) { … } })`: получает помощники общего моста и
 *   возвращает методы фичи. Методы принимают до двух строк и отвечают `OK:<данные>` или `ERR:<код>`.
 * @param failure превращает код ошибки (всё после `ERR:`) в исключение фичи.
 */
class FolderBridge(
    private val name: String,
    private val requiredFolder: String,
    private val script: String,
    private val failure: (code: String) -> Throwable,
) {
    private var installed = false

    /** Умеет ли браузер работать с папками на диске. */
    val isSupported: Boolean by lazy {
        install()
        isFileSystemAccessSupported()
    }

    /** Текущее состояние доступа к сохранённой папке. */
    suspend fun access(): FolderAccess {
        if (!isSupported) return FolderAccess.Unsupported
        val status = call("status")
        return when (status.substringBefore(':')) {
            "granted" -> FolderAccess.Granted(status.substringAfter(':'))
            "prompt" -> FolderAccess.NeedsPermission(status.substringAfter(':'))
            else -> FolderAccess.NotSelected
        }
    }

    /** Открывает выбор папки и запоминает её, если в ней есть [requiredFolder]. */
    suspend fun selectFolder() = FolderAccess.Granted(call("pick"))

    /** Снова спрашивает разрешение на папку, выбранную раньше. */
    suspend fun requestPermission() = FolderAccess.Granted(call("request"))

    /** Вызывает метод моста и возвращает данные после `OK:` или бросает исключение из [failure]. */
    suspend fun call(method: String, first: String = "", second: String = ""): String {
        install()
        val result = callBridge(name, method, first, second).await<JsString>().toString()
        if (result.startsWith("OK:")) return result.removePrefix("OK:")
        throw failure(result.removePrefix("ERR:"))
    }

    private fun install() {
        if (installed) return
        evaluate(FOLDER_BRIDGE_SOURCE)
        evaluate("globalThis.__bdoFolder.register('$name', '$requiredFolder', ($script))")
        installed = true
    }
}
